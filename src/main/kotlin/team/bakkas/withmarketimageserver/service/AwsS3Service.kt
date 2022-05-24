package team.bakkas.withmarketimageserver.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.PutObjectRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import team.bakkas.withmarketimageserver.entity.AwsS3
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.*

/** 계정과 연동된 Amazon S3와 관련된 서비스를 제공하는 클래스
 * @author Brian
 * @param amazonS3 Spring Configuration으로 등록된 AmazonS3 client 객체
 * @param bucket Bucket name of registered user's Amazon S3
 * @since 22/05/23
 */
@Service
class AwsS3Service(
    private val amazonS3: AmazonS3,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucket: String
) {

    /** multipartFile로 날아온 request file을 시스템 내부의 file로 변환시켜서 upload한 뒤, 업로드 정보를 반환하는 메소드
     * @param multipartFile http request multipartFile
     * @param dirName directory name
     */
    fun upload(multipartFile: MultipartFile, dirName: String): AwsS3 {
        val file = convertMultipartFileToFile(multipartFile) ?: throw IllegalArgumentException("MultipartFile -> FIle convert Exception")

        return upload(file, dirName)
    }

    /** key 정보와 path가 담긴 awsS3 객체를 파라미터로 받아서, 해당하는 파일을 S3 Storage에서 삭제하는 메소드
     * @param awsS3 key, poth가 담긴 객체
     */
    fun remove(awsS3: AwsS3): Unit {
        if(!amazonS3.doesObjectExist(bucket, awsS3.key))
            throw AmazonS3Exception("Object " + awsS3.key + " does not exist!")

        amazonS3.deleteObject(bucket, awsS3.key)
    }

    /** 파일을 AWS S3 상에 업로드시키는 메소드. 아래에 정의된 메소드들을 활용한다.
     * @param file 저장하고자하는 파일
     * @param dirName 저장하고자하는 디렉토리의 이름
     */
    private fun upload(file: File, dirName: String): AwsS3 {
        val key = randomFileName(file, dirName)
        val path = putS3(file, key) // key값을 이용해서 파일을 등록하고, 해당 url을 저장한다
        removeFile(file)

        return AwsS3(key, path)
    }

    /** 파일 이름을 랜덤으로 지정하는 메소드
     * @param file 올리고자하는 파일
     * @param dirName 디렉토리의 이름
     * @return 해당 파일에 대응하는 key값 (generated file name)
     */
    private fun randomFileName(file: File, dirName: String): String = dirName + "/" + UUID.randomUUID() + file.name

    /** Amazon S3로부터 해당 파일의 url을 가져오는 메소드
     * @param bucket 연동 계정에 등록되어있는 버킷의 이름
     * @param fileName 파일을 가져오기위한 key값 (random generated file name)
     * @return url of file registered at Amazon S3
     */
    private fun getS3(bucket: String, fileName: String): String = amazonS3.getUrl(bucket, fileName).toString()

    /** Amazon S3에 파일을 집어넣는 메소드
     * @param uploadFile 업로드하고자 하는 파일
     * @param fileName 파일의 이름. S3에는 random으로 지정된 이름으로 넣어서 파일 이름을 고유하게 만든다
     * @return url of file registered at Amazon S3
     */
    private fun putS3(uploadFile: File, fileName: String): String {
        amazonS3.putObject(PutObjectRequest(bucket, fileName, uploadFile)
            .withCannedAcl(CannedAccessControlList.PublicRead))

        return getS3(bucket, fileName)
    }

    /** 서버에 캐싱된 파일을 제거해버리는 메소드. S3에 업로드가 완료되는 즉시 실행해야하는 메소드
     * @param file 업로드하고자하는 파일
     */
    private fun removeFile(file: File): Unit {
        file.delete()
    }

    /** request에 실린 multipartFile을 system 상에 올린 뒤, 해당하는 file을 리턴해주는 메소드
     * @param multipartFile http request로 전달된 multipart File
     * @return file nullable File
     */
    private fun convertMultipartFileToFile(multipartFile: MultipartFile): File? {
        val file = File(System.getProperty("user.dir") + "/" + multipartFile.originalFilename)

        if (file.createNewFile()) {
            try {
                val fos = FileOutputStream(file)
                fos.write(multipartFile.bytes)
            } catch (e: IOException) {
                throw IOException()
            }

            return file
        }

        return null
    }
}
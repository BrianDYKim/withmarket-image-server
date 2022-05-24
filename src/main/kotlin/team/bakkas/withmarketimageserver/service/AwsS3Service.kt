package team.bakkas.withmarketimageserver.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
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

    /** 이미지를 업로드하는 메소드
     * @param multipartFiles request multipart로 날아오는 이미지 데이터들
     * @return list of fileName
     */
    fun uploadImages(multipartFiles: List<MultipartFile>): List<String> {
        val fileNameList = mutableListOf<String>()

        multipartFiles.forEach { file ->
            val fileName: String = createRandomFileName(file.originalFilename!!)
            val objectMetadata = ObjectMetadata()
            with(objectMetadata) {
                this.contentLength = file.size
                this.contentType = file.contentType
            }

            putS3(file, fileName, objectMetadata)

            fileNameList.add(fileName)
        }

        return fileNameList
    }

    /** fileName에 해당하는 image의 url을 반환하는 메소드
     * @param fileName 파일의 이름
     * @return url of image file
     * @throws AmazonS3Exception
     */
    fun getImageUrl(fileName: String): String {
        if(amazonS3.doesObjectExist(bucket, fileName))
            return "https://$bucket.s3.ap-northeast-2.amazonaws.com/$fileName"
        else
            throw AmazonS3Exception("file $fileName does not exist!")
    }

    /** fileName에 대응하는 Amazon S3에 저장된 이미지를 삭제시키는 메소드
     * @param fileName 파일의 이름
     */
    fun deleteImage(fileName: String) {
        amazonS3.deleteObject(DeleteObjectRequest(bucket, fileName))
    }

    /** UUID를 기반으로 랜덤한 이미지의 이름을 지정하는 메소드
     * @param fileName 파일의 이름
     * @return random name of image file
     */
    private fun createRandomFileName(fileName: String): String = UUID.randomUUID().toString() + getFileExtension(fileName)

    /** 파일의 확장자를 반환하는 메소드
     * @param fileName 파일의 이름
     * @return 파일의 확장자 string
     * @throws ResponseStatusException
     */
    private fun getFileExtension(fileName: String): String {
        try {
            return fileName.substring(fileName.lastIndexOf("."))
        } catch (e: StringIndexOutOfBoundsException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일($fileName) 입니다.")
        }
    }

    /** inputStream을 이용해서 이미지 파일을 Amazon S3 상에 업로드시키는 메소드
     * @param file multipartFile
     * @param fileName 파일의 이름
     * @param objectMetadata file의 meta-data (content-type, content-length)
     * @throws ResponseStatusException
     */
    private fun putS3(file: MultipartFile, fileName: String, objectMetadata: ObjectMetadata): Unit {
        try {
            val inputStream = file.inputStream

            amazonS3.putObject(PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                .withCannedAcl(CannedAccessControlList.PublicRead))
        } catch (e: IOException) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다.")
        }
    }
}
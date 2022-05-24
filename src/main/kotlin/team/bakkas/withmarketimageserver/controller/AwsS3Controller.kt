package team.bakkas.withmarketimageserver.controller

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import team.bakkas.withmarketimageserver.entity.AwsS3
import team.bakkas.withmarketimageserver.service.AwsS3Service

/** AWS S3에 관한 service들에 대한 controller class
 * @param awsS3Service AWS S3의 비지니스 로직을 구현한 service layer의 class
 */
@RestController
@RequestMapping("/v1/s3")
class AwsS3Controller(
    private val awsS3Service: AwsS3Service
) {
    @PostMapping("/file-upload")
    fun upload(@RequestPart("file") multipartFile: MultipartFile): AwsS3 = awsS3Service.upload(multipartFile, "images")

    @DeleteMapping("/file-delete")
    fun remove(key: String, path: String): Unit {
        val awsS3 = AwsS3(key, path)

        awsS3Service.remove(awsS3)
    }
}
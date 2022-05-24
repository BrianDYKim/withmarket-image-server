package team.bakkas.withmarketimageserver.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import team.bakkas.withmarketimageserver.service.AwsS3Service

/** AWS S3에 관한 service들에 대한 controller class
 * @param awsS3Service AWS S3의 비지니스 로직을 구현한 service layer의 class
 */
@RestController
@RequestMapping("/v1/s3")
class AwsS3Controller(
    private val awsS3Service: AwsS3Service
) {

    @GetMapping("/image-url")
    fun getImageUrl(@RequestParam(name = "name") fileName: String) = awsS3Service.getImageUrl(fileName)

    @PostMapping("/image-upload")
    fun uploadImage(@RequestPart multipartFile: List<MultipartFile>): ResponseEntity<List<String>> {
        return ResponseEntity.ok(awsS3Service.uploadImages(multipartFile))
    }

    @DeleteMapping("/image-delete")
    fun deleteImage(@RequestParam(name = "name") fileName: String): ResponseEntity<String> {
        awsS3Service.deleteImage(fileName)

        return ResponseEntity.ok("Success to delete $fileName")
    }
}
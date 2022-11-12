package team.bakkas.withmarketimageserver.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import team.bakkas.withmarketimageserver.service.AwsS3Service

/** AWS S3에 관한 service들에 대한 controller class
 * @param awsS3Service AWS S3의 비지니스 로직을 구현한 service layer의 class
 */
@RestController
@RequestMapping("/api/v1")
class AwsS3Controller(
    private val awsS3Service: AwsS3Service
) {

    @GetMapping("/image")
    fun getImageUrl(@RequestParam(name = "name") fileName: String) = awsS3Service.getImageUrl(fileName)

    /** multipart file을 받아서 해당 file을 업로드 후 주소를 반환해주는 메소드
     * @param multipartFile
     */
    @PostMapping("/image")
    fun uploadImage(@RequestPart multipartFile: List<MultipartFile>): ResponseEntity<List<String>> {
        return ResponseEntity.ok(awsS3Service.uploadImages(multipartFile))
    }

    @DeleteMapping("/image")
    fun deleteImage(@RequestParam(name = "name") fileName: String): ResponseEntity<String> {
        awsS3Service.deleteImage(fileName)

        return ResponseEntity.ok("Success to delete $fileName")
    }
}
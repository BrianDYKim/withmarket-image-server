package team.bakkas.withmarketimageserver.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import team.bakkas.withmarketimageserver.config.AwsS3Config

@Service
class AwsS3Service(
    private val awsS3Config: AwsS3Config,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucket: String
) {
    
}
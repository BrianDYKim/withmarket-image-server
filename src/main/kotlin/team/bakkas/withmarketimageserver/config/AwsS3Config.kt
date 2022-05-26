package team.bakkas.withmarketimageserver.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** AWS S3에 대한 Config를 수행하는 클래스. 정보들은 모두 config server로부터 가져온다.
 * @param accessKey S3 Full Access IAM에 대한 access-key 정보
 * @param secretKey S3 Full Access IAM에 대한 secret-key 정보
 * @param region Amazon S3를 이용하는 AWS 지역 정보
 */
@Configuration
class AwsS3Config(
    @Value("\${cloud.aws.credentials.access-key}")
    private val accessKey: String,
    @Value("\${cloud.aws.credentials.secret-key}")
    private val secretKey: String,
    @Value("\${cloud.aws.region.static}")
    private val region: String
) {

    @Bean
    fun amazonS3(): AmazonS3 = AmazonS3ClientBuilder.standard()
        .withRegion(region)
        .withCredentials(
            AWSStaticCredentialsProvider(
                BasicAWSCredentials(accessKey, secretKey)
            )
        )
        .build()
}
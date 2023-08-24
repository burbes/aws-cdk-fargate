package br.com.siecola.aws_project02.repository;

import br.com.siecola.aws_project02.model.ProductEventKey;
import br.com.siecola.aws_project02.model.ProductEventLog;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


@EnableScan // This annotation is used to allow the scan operation in the DynamoDB table (DynamoDbConfig)
public interface ProductEventLogRepository extends CrudRepository<ProductEventLog, ProductEventKey> {

    List<ProductEventLog> findAllByPk(String code);


    List<ProductEventLog> findAllByPkAndSkStartsWith(String code, String eventType);
}

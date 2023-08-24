package br.com.siecola.aws_project02.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;

public class ProductEventKey {

    public ProductEventKey() { }


    private String pk;

    private String sk;


    @DynamoDBHashKey(attributeName = "pk")
    public String getPk() {
        return pk;
    }

    @DynamoDBRangeKey(attributeName = "sk")
    public String getSk() {
        return sk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }


    public ProductEventKey(String pk, String sk) {
        this.pk = pk;
        this.sk = sk;
    }
}

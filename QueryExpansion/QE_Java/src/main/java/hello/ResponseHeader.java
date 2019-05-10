package hello;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "status",
        "QTime",
        "params"
})
public class ResponseHeader {

    @JsonProperty("status")
    public Integer status;
    @JsonProperty("QTime")
    public Integer qTime;
    @JsonProperty("params")
    public Params params;

}

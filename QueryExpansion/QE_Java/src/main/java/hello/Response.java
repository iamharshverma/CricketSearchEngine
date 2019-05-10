package hello;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "numFound",
        "start",
        "docs"
})
public class Response {

    @JsonProperty("numFound")
    public Integer numFound;
    @JsonProperty("start")
    public Integer start;
    @JsonProperty("docs")
    public List<Doc> docs = null;

}

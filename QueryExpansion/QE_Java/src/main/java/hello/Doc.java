package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "tstamp",
        "digest",
        "boost",
        "id",
        "title",
        "url",
        "_version_",
        "content"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Doc {

    @JsonProperty("tstamp")
    public String tstamp;
    @JsonProperty("digest")
    public String digest;
    @JsonProperty("boost")
    public Double boost;
    @JsonProperty("id")
    public String id;
    @JsonProperty("title")
    public String title;
    @JsonProperty("url")
    public String url;
    @JsonProperty("_version_")
    public String version;
    @JsonProperty("content")
    public String content;


}
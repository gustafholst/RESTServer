package se.miun.student.guho1700;

import java.util.Arrays;

public class Request {

    public enum Type {
        GET, POST, PUT, DELETE;

        public String getName() {
            return this.name();
        }

        public static Type stringToType(String str) throws RequestError {
            return Arrays.stream(values())
                    .filter(t -> t.getName().equals(str.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new RequestError("Request method not supported"));
        }
    }

    public Type method;
    public String path;
    public String payLoad;

    public static Request parseRequest(String rawText) throws RequestError {

        String[] lines = rawText.split("\r\n");

        String requestLine = lines[0];
        String[] requestLineSplit = requestLine.split(" ");

        //example line: "GET /foo/bar HTTP/2"
        String requestMethod = requestLineSplit[0];
        String path = requestLineSplit[1];

        Request request = new Request();
        request.method = Request.Type.stringToType(requestMethod);
        request.path = path;
        request.payLoad = lines[lines.length - 1];

        return request;
    }
}

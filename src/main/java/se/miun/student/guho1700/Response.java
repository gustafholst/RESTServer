package se.miun.student.guho1700;

public class Response {

    public enum Type {
        OK(200),
        CREATED(201),
        NOT_FOUND(404),
        ALREADY_EXISTS(409),
        INTERNAL_SERVER_ERROR(500);

        public final int code;

        public String getName() {
            return this.name();
        }

        Type(int code) {
            this.code = code;
        }
    }

    private Type responseType;
    private String responseBody;

    private int getBodyLength() {
        return responseBody == null ? 0 : responseBody.length();
    }

    //return header + body
    public String getResponse() {
        if (responseBody == null) {
            return getHeader();
        }
        return getHeader() + responseBody;
    }

    public String getHeader() {
        return "HTTP/2 " + this.responseType.code + " " + this.responseType.getName() + "\r\n" +
                "content-length: " + getBodyLength() + "\r\n" +
                "connection: close\r\n" +
                "\r\n";
    }

    public Response(Type type) {
        this(type, null);
    }

    public Response(Type type, String body) {
        this.responseType = type;
        this.responseBody = body;
    }

}

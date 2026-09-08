package backend.dto.response;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private final int status;
    private final T data;
    private final String message;
    private final LocalDateTime timestamp;


    public ApiResponse(
            int status,
            T data,
            String message
    ) {

        this.status = status;
        this.data = data;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }


    public int getStatus() {

        return status;
    }


    public T getData() {

        return data;
    }


    public String getMessage() {

        return message;
    }


    public LocalDateTime getTimestamp() {

        return timestamp;
    }
}
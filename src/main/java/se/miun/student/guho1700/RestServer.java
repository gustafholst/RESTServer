package se.miun.student.guho1700;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;

public class RestServer extends Thread {

    private ServerSocket serverSocket;
    private Disposable requestDisposable;
    private Connection currentConnection;
    private final DataBase db;

    public RestServer(final int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        db = new DataBase();
    }

    private static class Connection {
        public InputStream in;
        public OutputStream out;
    }

    @Override
    public void run() {
        requestDisposable = startListening()
                .doOnSubscribe(d -> log("Server listening on port: " + serverSocket.getLocalPort() + "\n\n"))
                .subscribeOn(Schedulers.io())
                .doOnNext(s -> log("Server received request: \n" + s + "\n--------------------------------------"))
                .map(Request::parseRequest)
                .map(this::handleRequest)
                .doOnDispose(()-> log("Server stopped"))
                .onErrorReturn(err -> new Response(Response.Type.INTERNAL_SERVER_ERROR)) // any error not handled
                .doFinally(this::closeConnection)
                .subscribe(this::sendResponse, Throwable::printStackTrace);
    }

    public Observable<String> startListening() {
        return Observable.<String>create(emitter -> {
            while (!emitter.isDisposed()) {
                try {
                    Socket socket = serverSocket.accept();

                    currentConnection = new Connection();
                    currentConnection.in = socket.getInputStream();
                    currentConnection.out = socket.getOutputStream();

                    byte[] buffer = new byte[2048];
                    int bytesRead = currentConnection.in.read(buffer);

                    emitter.onNext(new String(buffer));

                } catch (IOException ioe) {
                    emitter.onError(new Throwable(ioe.getMessage()));
                }
            }

            emitter.onComplete();

        }).publish().autoConnect();
    }

    private void log(String message) {
        System.out.println(message);
    }

    private void sendResponse(Response response) {
        try {
            byte[] msgBuffer = response.getResponse().getBytes(StandardCharsets.UTF_8);
            currentConnection.out.write(msgBuffer, 0, msgBuffer.length );
            currentConnection.out.flush();

            log("Sent response on thread: " + Thread.currentThread().getName());
        } catch (IOException e) {
            throw new IOError(e.getCause());
        }
    }

    private Response handleRequest(Request request) throws Throwable {
        switch (request.method) {
            case GET:
                return handleGetRequest(request);
            case POST:
                return handlePostRequest(request);
            case PUT:
                return handlePutRequest(request);
            case DELETE:
                return handleDeleteRequest(request);
            default:
                throw new Throwable("REST method not implemented");
        }
    }

    private Response handleGetRequest(Request request) throws IOException {
        try {
            Double val = db.fetchValue(request.path);
            return new Response(Response.Type.OK, String.valueOf(val));
        } catch (FileNotFoundException e) {
            return new Response(Response.Type.NOT_FOUND);
        }
    }

    private Response handlePostRequest(Request request) throws IOException {
        try {
            db.createNewSensor(request.payLoad);
            return new Response(Response.Type.CREATED);
        } catch (FileAlreadyExistsException e) {
            return new Response(Response.Type.ALREADY_EXISTS);
        }
    }

    private Response handlePutRequest(Request request) throws IOException {
        try {
            db.storeValue(request.path, Double.valueOf(request.payLoad));
            return new Response(Response.Type.OK);
        } catch (FileNotFoundException e) {
            return new Response(Response.Type.NOT_FOUND);
        }
    }

    private Response handleDeleteRequest(Request request) {
        try {
            db.deleteRecord(request.path);
            return new Response(Response.Type.OK);
        } catch (FileNotFoundException e) {
            return new Response(Response.Type.NOT_FOUND);
        }
    }

    public void closeConnection() throws IOException {
        currentConnection.out.flush();
        currentConnection.out.close();
        currentConnection.in.close();
    }

    public void shutDown() {
        if (!requestDisposable.isDisposed())
            requestDisposable.dispose();
    }
}

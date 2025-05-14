package com.github.ecc1esia.backend.config;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * 请求包装器类，用于包装HTTP请求以便于获取请求体
 */
@Slf4j
public class RequestWrapper extends HttpServletRequestWrapper {

    /**
     * 存储请求体的字符串变量
     */
    private final String body;

    /**
     * 构造函数，接收一个HttpServletRequest对象并包装它
     *
     * @param request 要包装的HttpServletRequest对象
     */
    public RequestWrapper(HttpServletRequest request) {
        super(request);

        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = request.getInputStream(); BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            char[] charBuffer = new char[128];

            int byteRead = -1;

            // 读取请求体并将其追加到StringBuilder中
            while ((byteRead = bufferedReader.read(charBuffer)) > 0) {
                stringBuilder.append(charBuffer, 0, byteRead);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        body = stringBuilder.toString();
    }

    /**
     * 重写getInputStream方法，返回一个包含请求体的ServletInputStream
     *
     * @return 包含请求体的ServletInputStream
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() throws IOException {
                return 0;
            }
        };
    }

    /**
     * 重写getReader方法，返回一个包含请求体的BufferedReader
     *
     * @return 包含请求体的BufferedReader
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    /**
     * 获取请求体的字符串表示
     *
     * @return 请求体的字符串表示
     */
    public String getBody() {
        return this.body;
    }
}

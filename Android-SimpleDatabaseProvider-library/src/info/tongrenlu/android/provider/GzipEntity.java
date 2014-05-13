package info.tongrenlu.android.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.util.EntityUtils;

public class GzipEntity extends HttpEntityWrapper {

    public static String entityToString(final HttpEntity entity, String charset) throws IOException {
        String result = null;
        if (isGzip(entity)) {
            result = EntityUtils.toString(new GzipEntity(entity), charset);
        } else {
            result = EntityUtils.toString(entity, charset);
        }
        return result;
    }

    private static boolean isGzip(final HttpEntity entity) {
        final Header contentEncodingHeader = entity.getContentEncoding();
        if (contentEncodingHeader != null) {
            final String contentEncoding = contentEncodingHeader.getValue();
            if (StringUtils.equalsIgnoreCase(contentEncoding, "gzip")) {
                return true;
            }
        }
        return false;
    }

    public GzipEntity(HttpEntity wrapped) {
        super(wrapped);
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {

        // the wrapped entity's getContent() decides about
        // repeatability
        final InputStream wrappedin = this.wrappedEntity.getContent();

        return new GZIPInputStream(wrappedin);
    }

    @Override
    public long getContentLength() {
        // length of ungzipped content not known in advance
        return -1;
    }

}

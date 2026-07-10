package com.ecommerce.inventory.service;

/**
 * Added in Version 6. Simple internal holder carrying an image's raw bytes together
 * with its resolved content type, so the controller can stream it back with the
 * correct Content-Type header. This is not a JSON DTO - GET /api/products/{id}/image
 * returns raw binary data, not a JSON body.
 */
public class ProductImageData {

    private final byte[] data;
    private final String contentType;

    public ProductImageData(byte[] data, String contentType) {
        this.data = data;
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }
}

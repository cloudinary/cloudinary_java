/**
 * The package holds classes used internally to implement verification procedures of authenticity and integrity of
 * client communication with Cloudinary servers. Verification is in most cases based on calculating and comparing so called
 * signatures, or hashed message authentication codes (HMAC) - string values calculated based on message payload, some
 * secret key value shared between communicating parties and SHA-1 hashing function.
 */
package com.cloudinary.api.signing;
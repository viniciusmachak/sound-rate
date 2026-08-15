package com.viniciusmcabral.sound_rate.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class StorageService {

	private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

	private final Cloudinary cloudinary;

	public StorageService(Cloudinary cloudinary) {
		this.cloudinary = cloudinary;
	}

	public String uploadFile(MultipartFile file) {
		try {
			String publicId = "avatars/" + UUID.randomUUID().toString();

			Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
					ObjectUtils.asMap("public_id", publicId, "overwrite", true));
			String secureUrl = (String) uploadResult.get("secure_url");
			logger.info("Uploaded avatar with public ID '{}'.", publicId);
			return secureUrl;
		} catch (IOException e) {
			logger.error("Failed to upload avatar: {}.", e.getMessage());
			throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
		}
	}
}

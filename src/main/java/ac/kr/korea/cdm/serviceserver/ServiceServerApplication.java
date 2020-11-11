package ac.kr.korea.cdm.serviceserver;

import ac.kr.korea.cdm.serviceserver.util.CryptoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

@SpringBootApplication
public class ServiceServerApplication {
	@Value("${publickey.type}")
	private String publicKeyType;

	public static void main(String[] args) {
		SpringApplication.run(ServiceServerApplication.class, args);
	}

	private static final Logger logger = LoggerFactory.getLogger(ServiceServerApplication.class);

	@PostConstruct
	public void init() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, BadPaddingException, IllegalBlockSizeException, IOException {
		CryptoHelper cryptoHelper = CryptoHelper.getInstance();
		if (!new File("SS-KeyPair").exists()) new File("SS-KeyPair").mkdir();
		if (!new File("SS-KeyPair/SS-PublicKey").exists() || !new File("SS-KeyPair/SS-PrivateKey").exists()) {
//            logger.info("create key pair");
//            KeyPair keyPair = cryptoHelper.generateEcKeyPair();
			KeyPair keyPair = null;
			if (publicKeyType.toLowerCase().trim().equals("ec")) keyPair = cryptoHelper.generateEcKeyPair();
			else if (publicKeyType.toLowerCase().trim().equals("rsa")) keyPair = cryptoHelper.generateRsaKeyPair();
			cryptoHelper.writeToFile(new File("SS-KeyPair/SS-PublicKey"), keyPair.getPublic().getEncoded());
			cryptoHelper.writeToFile(new File("SS-KeyPair/SS-PrivateKey"), keyPair.getPrivate().getEncoded());
		}
	}

}

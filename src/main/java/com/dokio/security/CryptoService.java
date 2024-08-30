package com.dokio.security;
import com.dokio.service.StorageService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;


@Service
public class CryptoService {

    Logger logger = Logger.getLogger(CryptoService.class);


    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private StorageService storageService;

    @Value("${files_path}")
    private String files_path;

    private static final String  ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";



    public SecretKey getKeyFromPassword(String password, String salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                .getEncoded(), "AES");
        return secret;
    }

    public IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        return new IvParameterSpec(iv);
    }

    public void encryptFile(SecretKey key, IvParameterSpec iv,
                                   InputStream inputStream, File outputFile) throws IOException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] buffer = new byte[64];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    outputStream.write(output);
                }
            }
            byte[] outputBytes = cipher.doFinal();
            if (outputBytes != null) {
                outputStream.write(outputBytes);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Exception in method encryptFile", ex);
        } finally {
            inputStream.close();
            outputStream.close();
        }
    }

    public String getMasterCryptoKey(Long masterId){
        String path = storageService.getBaseFilesFolderPath()+masterId.toString()+"//key.txt";
        try {
            if(!storageService.isPathExists(path)) createMasterKeyFile(masterId);
            String s = new String(Files.readAllBytes(Paths.get(path)));
            return s;
        } catch (IOException ex) {
            ex.printStackTrace();
            logger.error("Exception in method getCryptoKey in " + path, ex);
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Exception in method getCryptoKey in " + path, ex);
            return null;
        }
    }

    public SecretKey getSecretKeyByMasterCryptoPassword(Long masterId) throws Exception{
        String masterCryptoPassword = getCryptoPasswordFromDatabase(masterId);
        return getKeyFromPassword(masterCryptoPassword.substring(0, 32), masterCryptoPassword.substring(32));
    }

    // Key file "key.txt" contains key for encrypt and decrypt any information in the database encrypted fields, like password in crypto_password field for encrypt and decrypt files.
    // To get password for getKeyFromPassword() you need to get key from file "key.txt", then apply it to decrypt password from database.
    // By this way if only files will be leaked - it will be impossible to decrypt they because the password for AES algorithm in database on another server.
    // And if database is leaked - it will be impossible to decrypt encrypted data because key in files storage on another server.
    private void createMasterKeyFile(Long masterId) {
        Writer writer = null;
        String pathToMasterFolder = storageService.getBaseFilesFolderPath()+masterId.toString()+"//";
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(pathToMasterFolder+"key.txt"), "utf-8"));
            writer.write(UUID.randomUUID().toString());
        } catch (IOException ex) {
            ex.printStackTrace();
            logger.error("Exception in method createKeyFile in " + pathToMasterFolder, ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Exception iFromPassword(n method createKeyFile in " + pathToMasterFolder, ex);
        } finally {
            try {writer.close();} catch (Exception ex) {/*ignore*/}
        }
    }


    String getCryptoPasswordFromDatabase(Long masterId) throws Exception {
        String masterCryptoKey = getMasterCryptoKey(masterId);
        String stringQuery =
                        " select pgp_sym_decrypt(\"crypto_password\",'"+masterCryptoKey+"') " +
                        " from users " +
                        " where id = "+masterId+" and " +
                        " master_id = "+masterId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            String result = (String) query.getSingleResult();
            // If master user account is still not has crypto password - need to create it;
            if(Objects.isNull(result)){
                addCryptoPasswordToDatabase(masterId);
                result = (String) query.getSingleResult();
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Exception in method getCryptoPasswordFromDatabase. SQL=" + stringQuery, ex);
            return null;
//            throw new Exception();
        }
    }
    
    public void addCryptoPasswordToDatabase(Long masterId) throws Exception {
        String masterCryptoKey = getMasterCryptoKey(masterId);
        String stringQuery =
                " update users " +
                " set crypto_password = pgp_sym_encrypt(:generatedCryptoPassword, :masterCryptoKey) " +
                " where id = "+masterId+" and " +
                " master_id = "+masterId;
        try{
            Query query = entityManager.createNativeQuery(stringQuery);
            query.setParameter("generatedCryptoPassword", generateCryptoPassword(40)); // 32 for password and 8 for salt
            query.setParameter("masterCryptoKey", masterCryptoKey);
            query.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Exception in method addCryptoPasswordToDatabase. SQL=" + stringQuery, ex);
//            throw new Exception();
        }
    }

    // length up to 64 characters!
    private String generateCryptoPassword(int length){
        return (UUID.randomUUID().toString()+UUID.randomUUID().toString()).
        replaceAll("-", "").substring(0,length);
    }

}

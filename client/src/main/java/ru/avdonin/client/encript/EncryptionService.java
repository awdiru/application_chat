    package ru.avdonin.client.encript;

    import org.yaml.snakeyaml.Yaml;

    import javax.crypto.Cipher;
    import javax.crypto.spec.IvParameterSpec;
    import javax.crypto.spec.SecretKeySpec;
    import java.io.InputStream;
    import java.nio.charset.StandardCharsets;
    import java.security.SecureRandom;
    import java.util.Arrays;
    import java.util.Base64;
    import java.util.Map;
    import java.util.concurrent.ThreadLocalRandom;

    public class EncryptionService {
        private static final int IV_LENGTH = 16;
        private static final int CHAT_KEYS_LENGTH = 32;

        private String transformation;
        private String algorithm;
        private String secretKey;

        public EncryptionService() {
            loadPropertiesFromYaml();
        }

        private SecretKeySpec getSecretKeySpec() {
            if (secretKey == null || secretKey.length() != 32) {
                throw new IllegalArgumentException("Invalid key. Must be 32 chars");
            }
            return new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), algorithm);
        }

        private SecretKeySpec getSecretKeySpec(String key) {
            if (key == null || key.length() != 32) {
                throw new IllegalArgumentException("Invalid key. Must be 32 chars");
            }
            return new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
        }

        public String encrypt(String input) {
            try {
                Cipher cipher = Cipher.getInstance(transformation);
                IvParameterSpec ivSpec = generateIv();

                cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(), ivSpec);
                byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));

                return Base64.getEncoder().encodeToString(
                        concatenateByteArrays(ivSpec.getIV(), encrypted)
                );
            } catch (Exception e) {
                throw new RuntimeException("Encryption failed: ", e);
            }
        }

        public String encrypt(String input, String key) {
            try {
                Cipher cipher = Cipher.getInstance(transformation);
                IvParameterSpec ivSpec = generateIv();

                cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(key), ivSpec);
                byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));

                return Base64.getEncoder().encodeToString(
                        concatenateByteArrays(ivSpec.getIV(), encrypted)
                );
            } catch (Exception e) {
                throw new RuntimeException("Encryption failed: ", e);
            }
        }

        public String decrypt(String input) {
            try {
                byte[] decoded = Base64.getDecoder().decode(input);

                byte[] iv = Arrays.copyOfRange(decoded, 0, IV_LENGTH);
                byte[] cipherText = Arrays.copyOfRange(decoded, IV_LENGTH, decoded.length);

                Cipher cipher = Cipher.getInstance(transformation);
                cipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec(), new IvParameterSpec(iv));

                return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Decryption failed: ", e);
            }
        }

        public String decrypt(String input, String key) {
            try {
                byte[] decoded = Base64.getDecoder().decode(input);

                byte[] iv = Arrays.copyOfRange(decoded, 0, IV_LENGTH);
                byte[] cipherText = Arrays.copyOfRange(decoded, IV_LENGTH, decoded.length);

                Cipher cipher = Cipher.getInstance(transformation);
                cipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec(key), new IvParameterSpec(iv));

                return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Decryption failed: ", e);
            }
        }

        public String generateKey() {
            return ThreadLocalRandom.current()
                    .ints(32, 127)
                    .limit(CHAT_KEYS_LENGTH)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        }

        private IvParameterSpec generateIv() {
            byte[] array = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(array);
            return new IvParameterSpec(array);
        }

        private byte[] concatenateByteArrays(byte[] a, byte[] b) {
            byte[] result = new byte[a.length + b.length];
            System.arraycopy(a, 0, result, 0, a.length);
            System.arraycopy(b, 0, result, a.length, b.length);
            return result;
        }

        private void loadPropertiesFromYaml() {
            Yaml yaml = new Yaml();
            InputStream inputStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("application.yml");

            if (inputStream == null) {
                throw new RuntimeException("Файл application.yml не найден!");
            }
            Map<String, Object> yamlMap = yaml.load(inputStream);
            Map<String, Object> encryptionConfig = (Map<String, Object>) yamlMap.get("encryption");
            if (encryptionConfig != null) {
                transformation = (String) encryptionConfig.get("transformation");
                algorithm = (String) encryptionConfig.get("algorithm");
                secretKey = (String) encryptionConfig.get("key");
            } else {
                throw new RuntimeException("Раздел 'encryption' отсутствует в application.yml");
            }
        }
    }

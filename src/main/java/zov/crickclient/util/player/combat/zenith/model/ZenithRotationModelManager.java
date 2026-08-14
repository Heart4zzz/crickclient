package zov.crickclient.util.player.combat.zenith.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class ZenithRotationModelManager {
    private static final ZenithRotationModelManager INSTANCE = new ZenithRotationModelManager();

    public static ZenithRotationModelManager getInstance() {
        return INSTANCE;
    }

   public static final String rotationModelManagerText = "/assets/crickclient/models/particles/6d9e3f4a.dat";
   public static final String rotationModelManagerTextSecondary = "/assets/crickclient/models/rotation_model_v2.json";
   public static final String rotationModelManagerTextTertiary = "6d9e3f4a.dat";
   public static final String rotationModelManagerTextAlternate = "rotation_model_v2.json";
   public static final byte[] rotationModelManagerByteValueArray = new byte[]{90, 68, 76, 77, 2};
   public static final int rotationModelManagerValue = 16;
   public static final int rotationModelManagerValueSecondary = 12;
   public static final int rotationModelManagerValueTertiary = 256;
   public static final int rotationModelManagerValueAlternate = 128;
   public static final int rotationModelManagerValuePrevious = 73000;
   public static final SecureRandom rotationModelManagerSecureRandom = new SecureRandom();
   public static final String rotationModelManagerTextPrevious = "fc_";
   public static final int rotationModelManagerValueCurrent = 28;
   public static final int rotationModelManagerValueNext = 3;
   public static final int rotationModelManagerValueMinimum = 1000;
   public static final int rotationModelManagerValueMaximum = 15;
   public static final float rotationModelManagerAmount = 0.0F;
   public static final String[] rotationModelManagerTextArray = new String[]{
      "run/rotation_recordings/rotation_dataset_v2.csv",
      "run/rotation_recordings/rotation_dataset.csv",
      "rotation_recordings/rotation_dataset_v2.csv",
      "rotation_recordings/rotation_dataset.csv",
      "scripts/rotation_recordings/rotation_dataset_v2.csv",
      "scripts/rotation_recordings/rotation_dataset.csv",
      "rotation_dataset.csv",
      "C:\\source\\ZenithDLC\\run\\rotation_recordings\\rotation_dataset_v2.csv",
      "C:\\source\\ZenithDLC\\run\\rotation_recordings\\rotation_dataset.csv"
   };
   public static final String[] rotationModelManagerTextArraySecondary = new String[]{"target_Diffyaw_step", "target_diff_yaw_step", "target_yaw_step"};
   public static final String[] rotationModelManagerTextArrayTertiary = new String[]{"target_Diffpitch_step", "target_diff_pitch_step", "target_pitch_step"};
   public boolean rotationModelManagerEnabled = false;
   public ZenithRotationModel rotationModelManagerRotationModel;
   public int rotationModelManagerValueStart = 24;
   public final Deque<float[]> rotationModelManagerItems = new ArrayDeque<>();
   public final Deque<float[]> rotationModelManagerItemsSecondary = new ArrayDeque<>();
   public final Random rotationModelManagerRandom = new Random();
   public int rotationModelManagerValueEnd = -1;
   public int rotationModelManagerValueVariantA = -1;
   public int rotationModelManagerValueVariantB = -1;

   public boolean method00317() {
      return false;
   }

   public void method01076(String s) {
      if (this.method00317()) {
         // debug chat disabled
      }
   }

   public void method01162(String s) {
      if (this.method00317()) {
         System.out.println(s);
      }
   }

   public void method00556(String s) {
      if (this.method00317()) {
         System.err.println(s);
      }
   }

   public void method00643(Exception exception) {
      if (this.method00317()) {
         exception.printStackTrace();
      }
   }

   public void method00800() {
      try {
         for (File file1 : this.method00077()) {
            if (this.method00811(file1)) {
               this.method01076(file1.getAbsolutePath());
               return;
            }
         }

         if (this.method01053("/assets/crickclient/models/particles/6d9e3f4a.dat") || this.method01053("/assets/crickclient/models/rotation_model_v2.json")) {
            return;
         }

         this.method00556("[DeepLearning] No model found. Checked scripts/rotation_recordings, rotation_recordings, run and resources.");
      } catch (Exception var5) {
         this.method00556("[DeepLearning] Failed to load model: " + var5.getMessage());
         this.method00643(var5);
      }
   }

   public File[] method00077() {
      return new File[]{
         new File("scripts/rotation_recordings/6d9e3f4a.dat"),
         new File("rotation_recordings/6d9e3f4a.dat"),
         new File("run/rotation_recordings/6d9e3f4a.dat"),
         new File("run/6d9e3f4a.dat"),
         new File("6d9e3f4a.dat"),
         new File("C:\\source\\ZenithDLC\\scripts\\rotation_recordings\\6d9e3f4a.dat"),
         new File("scripts/rotation_recordings/rotation_model_v2.json"),
         new File("rotation_recordings/rotation_model_v2.json"),
         new File("run/rotation_recordings/rotation_model_v2.json"),
         new File("run/rotation_model_v2.json"),
         new File("rotation_model_v2.json"),
         new File("C:\\source\\ZenithDLC\\scripts\\rotation_recordings\\rotation_model_v2.json")
      };
   }

   public boolean method00811(File file1) throws IOException {
      if (file1 != null && file1.exists() && file1.isFile()) {
         this.method00643(file1);
         return true;
      } else {
         return false;
      }
   }

   public boolean method01053(String s) throws IOException {
      URL url = this.getClass().getResource(s);
      if (url == null) {
         return false;
      } else {
         boolean flag;
         try (InputStream inputstream = url.openStream()) {
            this.method01162("[DeepLearning] Loading model from resource: " + s);
            this.method00280(inputstream, this.method00643(url));
            flag = true;
         }

         return flag;
      }
   }

   public void method00962(InputStream inputstream) throws IOException {
      this.method00280(inputstream, null);
   }

   public void method00643(File file1) throws IOException {
      if (file1 != null && file1.exists() && file1.isFile()) {
         try (FileInputStream fileinputstream = new FileInputStream(file1)) {
            this.method01162("[DeepLearning] Loading model from: " + file1.getPath());
            this.method00280(fileinputstream, file1);
         }
      } else {
         throw new IOException("Model file not found");
      }
   }

   public void method00280(InputStream inputstream, File file1) throws IOException {
      byte[] abyte = inputstream.readAllBytes();
      boolean flag = this.method00553(abyte);
      String s = this.method00843(abyte);
      this.method00485(s);
      if (!flag) {
         this.method00280(file1, abyte);
      }
   }

   public void method00485(String s) {
      Gson gson = new Gson();
      JsonObject jsonobject = (JsonObject)gson.fromJson(s, JsonObject.class);
      this.rotationModelManagerZenithRotationModel = this.method00167(jsonobject);
      this.rotationModelManagerEnabled = true;
      this.method00423();
      this.method01162(
         "[DeepLearning] Model loaded successfully (direct GRU MoE"
            + (this.rotationModelManagerZenithRotationModel.rotationModelEnabledTertiary ? ", split heads" : "")
            + (this.rotationModelManagerZenithRotationModel.rotationModelEnabledSecondary ? ", two-head" : "")
            + ")"
      );
      this.method01162("[DeepLearning] Input size: " + this.rotationModelManagerZenithRotationModel.rotationModelValue);
      this.method01162("[DeepLearning] Sequence length: " + this.rotationModelManagerValueStart);
      this.method01162("[DeepLearning] Hidden size: " + this.rotationModelManagerZenithRotationModel.rotationModelValueSecondary);
      this.method01162("[DeepLearning] FC size: " + this.rotationModelManagerZenithRotationModel.rotationModelValueAlternate);
      this.method01162("[DeepLearning] Output size: " + this.rotationModelManagerZenithRotationModel.rotationModelValueCurrent);
      this.method01162(
         "[DeepLearning] MoE experts: "
            + this.rotationModelManagerZenithRotationModel.rotationModelValueNext
            + " (fixed idle: "
            + this.rotationModelManagerZenithRotationModel.rotationModelEnabled
            + ")"
      );
      this.method01162("[DeepLearning] Norm type: " + this.rotationModelManagerZenithRotationModel.rotationModelText);
      this.method00906();
   }

   public String method00843(byte[] abyte) throws IOException {
      if (this.method00553(abyte)) {
         return this.method00692(abyte);
      } else {
         String s = new String(abyte, StandardCharsets.UTF_8);
         if (!this.method00673(s)) {
            throw new IOException("Unsupported model payload format");
         } else {
            return s;
         }
      }
   }

   public void method00280(File file1, byte[] abyte) throws IOException {
      if (file1 != null && file1.exists() && file1.isFile() && file1.canWrite()) {
         byte[] abyte1 = this.method00591(this.method00765(abyte));
         Files.write(file1.toPath(), abyte1);
         this.method01162("[DeepLearning] Plain model encrypted in-place: " + file1.getPath());
      }
   }

   public byte[] method00765(byte[] abyte) throws IOException {
      String s = new String(abyte, StandardCharsets.UTF_8);

      try {
         JsonObject jsonobject = (JsonObject)new Gson().fromJson(s, JsonObject.class);
         if (jsonobject != null && jsonobject.has("config") && jsonobject.get("config").isJsonObject()) {
            JsonObject jsonobject1 = jsonobject.getAsJsonObject("config");
            if (jsonobject1.has("feature_columns") && jsonobject1.get("feature_columns").isJsonArray()) {
               JsonArray jsonarray = jsonobject1.getAsJsonArray("feature_columns");
               JsonArray jsonarray1 = new JsonArray();

               for (int i = 0; i < jsonarray.size(); i++) {
                  String s1 = jsonarray.get(i).getAsString();
                  jsonarray1.add(this.method00623(s1) ? s1 : this.method00871(s1));
               }

               jsonobject1.add("feature_columns", jsonarray1);
               return jsonobject.toString().getBytes(StandardCharsets.UTF_8);
            } else {
               return abyte;
            }
         } else {
            return abyte;
         }
      } catch (RuntimeException var9) {
         throw new IOException("Failed to obfuscate model feature names", var9);
      }
   }

   public boolean method00553(byte[] abyte) {
      if (abyte != null && abyte.length > rotationModelManagerByteValueArray.length + 16 + 12) {
         for (int i = 0; i < rotationModelManagerByteValueArray.length; i++) {
            if (abyte[i] != rotationModelManagerByteValueArray[i]) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public String method00692(byte[] abyte) throws IOException {
      try {
         int i = rotationModelManagerByteValueArray.length;
         byte[] abyte1 = Arrays.copyOfRange(abyte, i, i + 16);
         i += 16;
         byte[] abyte2 = Arrays.copyOfRange(abyte, i, i + 12);
         i += 12;
         byte[] abyte3 = Arrays.copyOfRange(abyte, i, abyte.length);
         Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
         cipher.init(2, this.method00110(abyte1), new GCMParameterSpec(128, abyte2));
         return new String(cipher.doFinal(abyte3), StandardCharsets.UTF_8);
      } catch (GeneralSecurityException | IllegalArgumentException var7) {
         throw new IOException("Failed to decrypt model payload", var7);
      }
   }

   public byte[] method00591(byte[] abyte) throws IOException {
      try {
         byte[] abyte1 = new byte[16];
         byte[] abyte2 = new byte[12];
         rotationModelManagerSecureRandom.nextBytes(abyte1);
         rotationModelManagerSecureRandom.nextBytes(abyte2);
         Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
         cipher.init(1, this.method00110(abyte1), new GCMParameterSpec(128, abyte2));
         byte[] abyte3 = cipher.doFinal(abyte);
         ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(
            rotationModelManagerByteValueArray.length + abyte1.length + abyte2.length + abyte3.length
         );
         bytearrayoutputstream.write(rotationModelManagerByteValueArray);
         bytearrayoutputstream.write(abyte1);
         bytearrayoutputstream.write(abyte2);
         bytearrayoutputstream.write(abyte3);
         return bytearrayoutputstream.toByteArray();
      } catch (GeneralSecurityException var7) {
         throw new IOException("Failed to encrypt model payload", var7);
      }
   }

   public SecretKeySpec method00110(byte[] abyte) throws GeneralSecurityException {
      char[] achar = this.method01115();

      SecretKeySpec secretkeyspec;
      try {
         PBEKeySpec pbekeyspec = new PBEKeySpec(achar, abyte, 73000, 256);
         byte[] abyte1 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(pbekeyspec).getEncoded();
         secretkeyspec = new SecretKeySpec(abyte1, "AES");
      } finally {
         Arrays.fill(achar, '\u0000');
      }

      return secretkeyspec;
   }

   public char[] method01115() {
      int[] aint = new int[]{
         23,
         15,
         233,
         205,
         181,
         182,
         191,
         340,
         374,
         360,
         285,
         483,
         477,
         423,
         407,
         617,
         626,
         596,
         634,
         514,
         675,
         660,
         678,
         647,
         865,
         839,
         851,
         881,
         783,
         1015,
         966,
         956,
         921,
         1072,
         1045,
         1140,
         1107,
         1096
      };
      char[] achar = new char[aint.length];

      for (int i = 0; i < aint.length; i++) {
         achar[i] = (char)(aint[i] ^ 77 + i * 29);
      }

      return achar;
   }

   public String method00871(String s) {
      return "fc_" + Long.toUnsignedString(this.method01260(s), 16);
   }

   public long method01260(String s) {
      String s1 = this.method01171(s);

      try {
         MessageDigest messagedigest = MessageDigest.getInstance("SHA-256");
         char[] achar = this.method01115();

         try {
            for (char c0 : achar) {
               messagedigest.update((byte)(c0 >>> '\b'));
               messagedigest.update((byte)c0);
            }
         } finally {
            Arrays.fill(achar, '\u0000');
         }

         byte[] var14 = s1.getBytes(StandardCharsets.UTF_8);
         messagedigest.update((byte)124);
         messagedigest.update(var14);
         byte[] var15 = messagedigest.digest();
         long var16 = 0L;

         for (int i = 0; i < 8; i++) {
            var16 = var16 << 8 | var15[i] & 255L;
         }

         return var16;
      } catch (GeneralSecurityException var13) {
         throw new IllegalStateException("Failed to hash model feature name", var13);
      }
   }

   public boolean method00623(String s) {
      if (s != null && s.startsWith("fc_") && s.length() > "fc_".length()) {
         for (int i = "fc_".length(); i < s.length(); i++) {
            char c0 = s.charAt(i);
            boolean flag = c0 >= '0' && c0 <= '9' || c0 >= 'a' && c0 <= 'f' || c0 >= 'A' && c0 <= 'F';
            if (!flag) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean method00673(String s) {
      if (s == null) {
         return false;
      } else {
         for (int i = 0; i < s.length(); i++) {
            char c0 = s.charAt(i);
            if (c0 != '\ufeff' && !Character.isWhitespace(c0)) {
               return c0 == '{';
            }
         }

         return false;
      }
   }

   public File method00643(URL url) {
      if (url != null && "file".equalsIgnoreCase(url.getProtocol())) {
         try {
            return new File(url.toURI());
         } catch (Exception var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static String method00847(String... astring) {
      StringBuilder stringbuilder = new StringBuilder();

      for (String s : astring) {
         stringbuilder.append('/').append(s);
      }

      return stringbuilder.toString();
   }

   public void method00423() {
      this.rotationModelManagerItems.clear();
      this.method00402();
   }

   public void method00578() {
      this.rotationModelManagerItemsSecondary.clear();
      this.method00402();
   }

   public void method00402() {
      this.rotationModelManagerValueEnd = -1;
      this.rotationModelManagerValueVariantA = -1;
      this.rotationModelManagerValueVariantB = -1;
   }

   public float[] method00280(float[] afloat) {
      ZenithRotationPrediction readableclass0236$inner0129 = this.method00811(afloat);
      return readableclass0236$inner0129 == null ? null : readableclass0236$inner0129.method00887();
   }

   public int method00395() {
      return this.rotationModelManagerZenithRotationModel == null ? -1 : this.rotationModelManagerZenithRotationModel.rotationModelValue;
   }

   public ZenithRotationPrediction method00811(float[] afloat) {
      if (this.rotationModelManagerEnabled && this.rotationModelManagerZenithRotationModel != null && afloat != null) {
         if (afloat.length != this.rotationModelManagerZenithRotationModel.rotationModelValue) {
            this.method00556(
               "[DeepLearning] Invalid feature count: " + afloat.length + ", expected " + this.rotationModelManagerZenithRotationModel.rotationModelValue
            );
            return null;
         } else {
            this.rotationModelManagerItems.addLast((float[])afloat.clone());

            while (this.rotationModelManagerItems.size() > this.rotationModelManagerValueStart) {
               this.rotationModelManagerItems.removeFirst();
            }

            return this.rotationModelManagerItems.size() < this.rotationModelManagerValueStart ? null : this.method00811(this.method00470());
         }
      } else {
         return null;
      }
   }

   public float[] method00643(float[] afloat) {
      if (this.rotationModelManagerEnabled && this.rotationModelManagerZenithRotationModel != null) {
         if (afloat != null) {
            if (afloat.length != this.rotationModelManagerZenithRotationModel.rotationModelValue) {
               return null;
            }

            this.rotationModelManagerItems.addLast((float[])afloat.clone());

            while (this.rotationModelManagerItems.size() > this.rotationModelManagerValueStart) {
               this.rotationModelManagerItems.removeFirst();
            }
         }

         return this.rotationModelManagerItems.size() < this.rotationModelManagerValueStart ? null : this.method00811(this.method00470()).method00887();
      } else {
         return null;
      }
   }

   public float[] method00962(float[] afloat) {
      if (this.rotationModelManagerEnabled && this.rotationModelManagerZenithRotationModel != null) {
         if (afloat != null) {
            if (afloat.length != this.rotationModelManagerZenithRotationModel.rotationModelValue) {
               return null;
            }

            this.rotationModelManagerItemsSecondary.addLast((float[])afloat.clone());

            while (this.rotationModelManagerItemsSecondary.size() > this.rotationModelManagerValueStart) {
               this.rotationModelManagerItemsSecondary.removeFirst();
            }
         }

         if (this.rotationModelManagerItemsSecondary.size() < this.rotationModelManagerValueStart) {
            return null;
         } else {
            float[][] afloat1 = new float[this.rotationModelManagerValueStart][this.rotationModelManagerZenithRotationModel.rotationModelValue];
            int i = 0;

            for (float[] afloat2 : this.rotationModelManagerItemsSecondary) {
               afloat1[i++] = afloat2;
            }

            return this.method00811(afloat1).method00887();
         }
      } else {
         return null;
      }
   }

   public float[][] method00470() {
      float[][] afloat = new float[this.rotationModelManagerValueStart][this.rotationModelManagerZenithRotationModel.rotationModelValue];
      int i = 0;

      for (float[] afloat1 : this.rotationModelManagerItems) {
         afloat[i++] = afloat1;
      }

      return afloat;
   }

   public float[] method00280(float[][] afloat) {
      return this.method00811(afloat).method00887();
   }

   public ZenithRotationPrediction method00811(float[][] afloat) {
      float[][] afloat1 = new float[this.rotationModelManagerValueStart][this.rotationModelManagerZenithRotationModel.rotationModelValuePrevious];

      for (int i = 0; i < this.rotationModelManagerValueStart; i++) {
         float[] afloat2 = this.method00280(afloat[i], this.rotationModelManagerZenithRotationModel.rotationModelDenseLayer);
         float[] afloat3 = this.method00280(
            afloat2, this.rotationModelManagerZenithRotationModel.rotationModelAmountArray, this.rotationModelManagerZenithRotationModel.rotationModelAmountArraySecondary
         );
         afloat3 = this.method00280(afloat3, this.rotationModelManagerZenithRotationModel.rotationModelNormalizationLayer);
         afloat1[i] = this.method00847(afloat3);
      }

      float[] afloat6 = this.method00643(afloat1);
      float[] afloat7 = this.method01105(
         this.method00280(
            afloat6,
            this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayTertiary,
            this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayAlternate
         )
      );
      float[] afloat8 = this.method01105(
         this.method00280(
            afloat7,
            this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayPrevious,
            this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayCurrent
         )
      );
      if (this.rotationModelManagerZenithRotationModel.rotationModelEnabledTertiary) {
         ZenithRotationModelHeader readableclass0236$inner0130 = this.method00280(
            afloat8,
            this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayEnd,
            this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayVariantA,
            this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayVariantD,
            this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayVariantE,
            this.rotationModelManagerValueEnd
         );
         ZenithRotationModelHeader readableclass0236$inner01301 = this.method00280(
            afloat8,
            this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayVariantB,
            this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayVariantC,
            this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayVariantF,
            this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayVariantG,
            this.rotationModelManagerValueVariantA
         );
         if (readableclass0236$inner0130.rotationModelHeaderEnabled && readableclass0236$inner01301.rotationModelHeaderEnabled) {
            this.rotationModelManagerValueEnd = readableclass0236$inner0130.rotationModelHeaderValue;
            this.rotationModelManagerValueVariantA = readableclass0236$inner01301.rotationModelHeaderValue;
            float f1 = this.rotationModelManagerZenithRotationModel.rotationModelEnabledSecondary
               ? this.method00375(
                  this.method00280(
                     afloat8,
                     this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayVariantH,
                     this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayVariantI
                  )[0]
               )
               : 1.0F;
            float f2 = this.rotationModelManagerZenithRotationModel.rotationModelEnabledSecondary
               ? this.method00375(
                  this.method00280(
                     afloat8,
                     this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayVariantJ,
                     this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayVariantK
                  )[0]
               )
               : 1.0F;
            float f3 = readableclass0236$inner0130.rotationModelHeaderAmount * f1;
            float f4 = readableclass0236$inner01301.rotationModelHeaderAmount * f2;
            return this.isFinite(f3) && this.isFinite(f4) && this.isFinite(f1) && this.isFinite(f2)
               ? new ZenithRotationPrediction(
                  f3,
                  f4,
                  f1,
                  f2,
                  readableclass0236$inner0130.rotationModelHeaderAmount,
                  readableclass0236$inner01301.rotationModelHeaderAmount,
                  readableclass0236$inner0130.rotationModelHeaderAmountArray,
                  readableclass0236$inner01301.rotationModelHeaderAmountArray,
                  readableclass0236$inner0130.rotationModelHeaderValue,
                  readableclass0236$inner01301.rotationModelHeaderValue
               )
               : this.method01197();
         } else {
            return this.method01197();
         }
      } else {
         float[] afloat4 = this.method00280(
            afloat8,
            this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayNext,
            this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayMinimum
         );
         if (afloat4.length != this.rotationModelManagerZenithRotationModel.rotationModelValueNext) {
            return this.method01197();
         } else {
            for (float f : afloat4) {
               if (!this.isFinite(f)) {
                  return this.method01197();
               }
            }

            float[] afloat9 = this.method00280(
               afloat8,
               this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayMaximum,
               this.rotationModelManagerZenithRotationModel.rotationModelAmountArrayStart
            );
            int l = (
                  this.rotationModelManagerZenithRotationModel.rotationModelEnabled
                     ? this.rotationModelManagerZenithRotationModel.rotationModelValueNext - 1
                     : this.rotationModelManagerZenithRotationModel.rotationModelValueNext
               )
               * 2;
            if (afloat9.length != l) {
               return this.method01197();
            } else {
               float[][] afloat10 = new float[this.rotationModelManagerZenithRotationModel.rotationModelValueNext][2];
               int i1 = 0;
               if (this.rotationModelManagerZenithRotationModel.rotationModelEnabled) {
                  afloat10[0][0] = 0.0F;
                  afloat10[0][1] = 0.0F;

                  for (int j = 1; j < this.rotationModelManagerZenithRotationModel.rotationModelValueNext; j++) {
                     afloat10[j][0] = afloat9[i1++];
                     afloat10[j][1] = afloat9[i1++];
                  }
               } else {
                  for (int j1 = 0; j1 < this.rotationModelManagerZenithRotationModel.rotationModelValueNext; j1++) {
                     afloat10[j1][0] = afloat9[i1++];
                     afloat10[j1][1] = afloat9[i1++];
                  }
               }

               float[] afloat11 = this.method00783(afloat4);
               int k = this.method00280(afloat4, this.rotationModelManagerValueVariantB);
               if (k >= 0 && k < afloat10.length) {
                  this.rotationModelManagerValueVariantB = k;
                  float[] afloat5 = this.method00280(afloat10, afloat11, k, this.method00811(afloat4, k));
                  return afloat5.length == 2 && this.isFinite(afloat5[0]) && this.isFinite(afloat5[1])
                     ? new ZenithRotationPrediction(afloat5[0], afloat5[1], 1.0F, 1.0F, afloat5[0], afloat5[1], afloat11, (float[])afloat11.clone(), k, k)
                     : this.method01197();
               } else {
                  return this.method01197();
               }
            }
         }
      }
   }

   public ZenithRotationModelHeader method00280(float[] afloat, float[][] afloat1, float[] afloat2, float[][] afloat3, float[] afloat4, int i) {
      float[] afloat5 = this.method00280(afloat, afloat1, afloat2);
      if (afloat5.length != this.rotationModelManagerZenithRotationModel.rotationModelValueNext) {
         return ZenithRotationModelHeader.method00392(this.rotationModelManagerZenithRotationModel.rotationModelValueNext);
      } else {
         float[] afloat6 = this.method00280(afloat, afloat3, afloat4);
         int j = this.rotationModelManagerZenithRotationModel.rotationModelEnabled
            ? this.rotationModelManagerZenithRotationModel.rotationModelValueNext - 1
            : this.rotationModelManagerZenithRotationModel.rotationModelValueNext;
         if (afloat6.length != j) {
            return ZenithRotationModelHeader.method00392(this.rotationModelManagerZenithRotationModel.rotationModelValueNext);
         } else {
            float[] afloat7 = new float[this.rotationModelManagerZenithRotationModel.rotationModelValueNext];
            if (this.rotationModelManagerZenithRotationModel.rotationModelEnabled) {
               afloat7[0] = 0.0F;

               for (int k = 1; k < this.rotationModelManagerZenithRotationModel.rotationModelValueNext; k++) {
                  afloat7[k] = afloat6[k - 1];
               }
            } else {
               System.arraycopy(afloat6, 0, afloat7, 0, this.rotationModelManagerZenithRotationModel.rotationModelValueNext);
            }

            float[] afloat8 = this.method00783(afloat5);
            int l = this.method00280(afloat5, i);
            if (l >= 0 && l < afloat7.length) {
               float f = this.method00280(afloat7, afloat8, l, this.method00811(afloat5, l));
               return new ZenithRotationModelHeader(f, afloat8, l, this.isFinite(f));
            } else {
               return ZenithRotationModelHeader.method00392(this.rotationModelManagerZenithRotationModel.rotationModelValueNext);
            }
         }
      }
   }

   public float[] method00280(float[] afloat, DenseLayer readableclass0236$inner0131) {
      float[] afloat1 = new float[afloat.length];

      for (int i = 0; i < afloat.length; i++) {
         float f = readableclass0236$inner0131.denseLayerAmountArraySecondary[i];
         if (Math.abs(f) < 1.0E-6F) {
            f = 1.0F;
         }

         afloat1[i] = (afloat[i] - readableclass0236$inner0131.denseLayerAmountArray[i]) / f;
      }

      return afloat1;
   }

   public float[] method00280(float[] afloat, NormalizationLayer readableclass0236$inner0133) {
      float f = 0.0F;

      for (float f1 : afloat) {
         f += f1;
      }

      f /= afloat.length;
      float f4 = 0.0F;

      for (float f2 : afloat) {
         float f3 = f2 - f;
         f4 += f3 * f3;
      }

      f4 /= afloat.length;
      float f5 = (float)(1.0 / Math.sqrt(f4 + 1.0E-5));
      float[] afloat1 = new float[afloat.length];

      for (int i = 0; i < afloat.length; i++) {
         float f6 = (afloat[i] - f) * f5;
         afloat1[i] = readableclass0236$inner0133.normalizationLayerAmountArray[i] * f6 + readableclass0236$inner0133.normalizationLayerAmountArraySecondary[i];
      }

      return afloat1;
   }

   public float[] method00643(float[][] afloat) {
      int i = this.rotationModelManagerZenithRotationModel.rotationModelValueTertiary;
      int j = this.rotationModelManagerZenithRotationModel.rotationModelValueSecondary;
      float[][] afloat1 = new float[i][j];

      for (int k = 0; k < this.rotationModelManagerValueStart; k++) {
         float[] afloat2 = afloat[k];

         for (int l = 0; l < i; l++) {
            OutputLayer readableclass0236$inner0134 = this.rotationModelManagerZenithRotationModel.rotationModelOutputLayerArray[l];
            float[] afloat3 = afloat1[l];
            int i1 = l == 0 ? this.rotationModelManagerZenithRotationModel.rotationModelValuePrevious : j;
            float[] afloat4 = new float[j];
            float[] afloat5 = new float[j];
            float[] afloat6 = new float[j];

            for (int j1 = 0; j1 < j; j1++) {
               float f = readableclass0236$inner0134.outputLayerAmountArrayTertiary[j1] + readableclass0236$inner0134.outputLayerAmountArrayAlternate[j1];
               float f1 = readableclass0236$inner0134.outputLayerAmountArrayTertiary[j + j1]
                  + readableclass0236$inner0134.outputLayerAmountArrayAlternate[j + j1];
               float f2 = readableclass0236$inner0134.outputLayerAmountArrayTertiary[2 * j + j1];
               float f3 = readableclass0236$inner0134.outputLayerAmountArrayAlternate[2 * j + j1];

               for (int k1 = 0; k1 < i1; k1++) {
                  f += readableclass0236$inner0134.outputLayerAmountArray[j1][k1] * afloat2[k1];
                  f1 += readableclass0236$inner0134.outputLayerAmountArray[j + j1][k1] * afloat2[k1];
                  f2 += readableclass0236$inner0134.outputLayerAmountArray[2 * j + j1][k1] * afloat2[k1];
               }

               for (int i2 = 0; i2 < j; i2++) {
                  f += readableclass0236$inner0134.outputLayerAmountArraySecondary[j1][i2] * afloat3[i2];
                  f1 += readableclass0236$inner0134.outputLayerAmountArraySecondary[j + j1][i2] * afloat3[i2];
                  f3 += readableclass0236$inner0134.outputLayerAmountArraySecondary[2 * j + j1][i2] * afloat3[i2];
               }

               afloat4[j1] = this.method00375(f);
               afloat5[j1] = this.method00375(f1);
               afloat6[j1] = (float)Math.tanh(f2 + afloat4[j1] * f3);
            }

            float[] afloat7 = new float[j];

            for (int l1 = 0; l1 < j; l1++) {
               afloat7[l1] = (1.0F - afloat5[l1]) * afloat6[l1] + afloat5[l1] * afloat3[l1];
            }

            afloat1[l] = afloat7;
            afloat2 = afloat7;
         }
      }

      return afloat1[i - 1];
   }

   public float[] method00280(float[] afloat, float[][] afloat1, float[] afloat2) {
      int i = afloat1.length;
      float[] afloat3 = new float[i];

      for (int j = 0; j < i; j++) {
         float f = afloat2[j];

         for (int k = 0; k < afloat.length; k++) {
            f += afloat1[j][k] * afloat[k];
         }

         afloat3[j] = f;
      }

      return afloat3;
   }

   public float[] method01105(float[] afloat) {
      float[] afloat1 = new float[afloat.length];

      for (int i = 0; i < afloat.length; i++) {
         afloat1[i] = (float)(0.5 * afloat[i] * (1.0 + method00198(afloat[i] / Math.sqrt(2.0))));
      }

      return afloat1;
   }

   public float[] method00847(float[] afloat) {
      float[] afloat1 = new float[afloat.length];

      for (int i = 0; i < afloat.length; i++) {
         float f = afloat[i];
         afloat1[i] = (float)(f / (1.0 + Math.exp(-f)));
      }

      return afloat1;
   }

   public static double method00198(double d0) {
      int i = d0 < 0.0 ? -1 : 1;
      d0 = Math.abs(d0);
      double d1 = 0.254829592;
      double d2 = -0.284496736;
      double d3 = 1.421413741;
      double d4 = -1.453152027;
      double d5 = 1.061405429;
      double d6 = 0.3275911;
      double d7 = 1.0 / (1.0 + d6 * d0);
      double d8 = 1.0 - ((((d5 * d7 + d4) * d7 + d3) * d7 + d2) * d7 + d1) * d7 * Math.exp(-d0 * d0);
      return i * d8;
   }

   public float method00375(float f) {
      return (float)(1.0 / (1.0 + Math.exp(-f)));
   }

   public int method00198(float[] afloat) {
      if (afloat != null && afloat.length != 0) {
         int i = 0;
         float f = afloat[0];

         for (int j = 1; j < afloat.length; j++) {
            if (afloat[j] > f) {
               f = afloat[j];
               i = j;
            }
         }

         return i;
      } else {
         return -1;
      }
   }

   public int method00280(float[] afloat, int i) {
      int j = this.method00198(afloat);
      if (j < 0) {
         return -1;
      } else if (i >= 0 && i < afloat.length && i != j) {
         return afloat[j] - afloat[i] >= 0.0F ? j : i;
      } else {
         return j;
      }
   }

   public int method00811(float[] afloat, int i) {
      if (afloat != null && afloat.length > 1) {
         int j = -1;
         float f = -Float.MAX_VALUE;

         for (int k = 0; k < afloat.length; k++) {
            if (k != i) {
               float f1 = afloat[k];
               if (j < 0 || f1 > f) {
                  j = k;
                  f = f1;
               }
            }
         }

         return j;
      } else {
         return -1;
      }
   }

   public float method00280(float[] afloat, float[] afloat1, int i, int j) {
      if (afloat != null && afloat1 != null && i >= 0 && i < afloat.length && i < afloat1.length) {
         float f = afloat1[i];
         float f1 = afloat[i] * f;
         float f2 = f;
         if (j >= 0 && j < afloat.length && j < afloat1.length) {
            float f3 = afloat1[j];
            f1 += afloat[j] * f3;
            f2 = f + f3;
         }

         return !(f2 <= 1.0E-6F) && this.isFinite(f2) ? f1 / f2 : afloat[i];
      } else {
         return 0.0F;
      }
   }

   public float[] method00280(float[][] afloat, float[] afloat1, int i, int j) {
      if (afloat != null && afloat1 != null && i >= 0 && i < afloat.length && i < afloat1.length) {
         float f = afloat1[i];
         float f1 = afloat[i][0] * f;
         float f2 = afloat[i][1] * f;
         float f3 = f;
         if (j >= 0 && j < afloat.length && j < afloat1.length) {
            float f4 = afloat1[j];
            f1 += afloat[j][0] * f4;
            f2 += afloat[j][1] * f4;
            f3 = f + f4;
         }

         return !(f3 <= 1.0E-6F) && this.isFinite(f3) ? new float[]{f1 / f3, f2 / f3} : afloat[i];
      } else {
         return new float[]{0.0F, 0.0F};
      }
   }

   public float[] method00783(float[] afloat) {
      float[] afloat1 = new float[afloat.length];
      if (afloat.length == 0) {
         return afloat1;
      } else {
         float f = afloat[0];

         for (int i = 1; i < afloat.length; i++) {
            f = Math.max(f, afloat[i]);
         }

         float f1 = 0.0F;

         for (int j = 0; j < afloat.length; j++) {
            afloat1[j] = (float)Math.exp(afloat[j] - f);
            f1 += afloat1[j];
         }

         if (!(f1 <= 0.0F) && this.isFinite(f1)) {
            for (int k = 0; k < afloat1.length; k++) {
               afloat1[k] /= f1;
            }

            return afloat1;
         } else {
            Arrays.fill(afloat1, 1.0F / afloat.length);
            return afloat1;
         }
      }
   }

   public boolean isFinite(float f) {
      return !Float.isNaN(f) && !Float.isInfinite(f);
   }

   public ZenithRotationPrediction method01197() {
      int i = this.rotationModelManagerZenithRotationModel == null ? 0 : this.rotationModelManagerZenithRotationModel.rotationModelValueNext;
      return new ZenithRotationPrediction(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, new float[i], new float[i], -1, -1);
   }

   public void method00906() {
      if (this.rotationModelManagerZenithRotationModel != null) {
         for (String s : rotationModelManagerTextArray) {
            File file1 = new File(s);
            if (file1.exists() && file1.isFile()) {
               try {
                  if (this.method00962(file1)) {
                     return;
                  }
               } catch (Exception var7) {
                  this.method00556("[DeepLearning] Model-check failed for dataset " + file1.getPath() + ": " + var7.getMessage());
               }
            }
         }

         this.method01162("[DeepLearning] Model-check skipped: rotation_dataset.csv not found.");
      }
   }

   public boolean method00962(File file1) throws IOException {
      if (this.rotationModelManagerZenithRotationModel.rotationModelTimestampArray != null
         && this.rotationModelManagerZenithRotationModel.rotationModelTimestampArray.length == this.rotationModelManagerZenithRotationModel.rotationModelValue) {
         try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(file1), StandardCharsets.UTF_8))) {
            String s = bufferedreader.readLine();
            if (s == null) {
               this.method01162("[DeepLearning] Model-check skipped: dataset is empty: " + file1.getPath());
               return false;
            }

            String[] astring = this.method00620(s);
            int[] aint = this.method00280(astring, this.rotationModelManagerZenithRotationModel.rotationModelTimestampArray);
            int i = this.method00280(astring, rotationModelManagerTextArraySecondary);
            int j = this.method00280(astring, rotationModelManagerTextArrayTertiary);
            int k = this.method00280(astring, new String[]{"session_id"});
            int l = this.method00280(astring, new String[]{"player_age"});
            int i1 = this.method00280(astring, new String[]{"timestamp_ms"});
            boolean flag = k >= 0 && l >= 0 && i1 >= 0;
            ArrayDeque<ActivationLayer> arraydeque = new ArrayDeque<>();
            HashSet hashset = new HashSet();
            int j1 = 0;
            int k1 = 1;

            String s1;
            while ((s1 = bufferedreader.readLine()) != null) {
               k1++;
               if (!s1.isEmpty()) {
                  String[] astring1 = this.method00620(s1);
                  float[] afloat = this.method00280(astring1, aint);
                  if (afloat != null) {
                     Long olong = flag ? this.method00643(astring1, k) : null;
                     Integer integer = flag ? this.method00811(astring1, l) : null;
                     Long olong1 = flag ? this.method00643(astring1, i1) : null;
                     if (!flag || olong != null && integer != null && olong1 != null) {
                        Float f = this.method00280(astring1, i);
                        Float f1 = this.method00280(astring1, j);
                        ActivationLayer readableclass0236$inner0132 = new ActivationLayer(afloat, olong, integer, olong1, f, f1, k1);
                        ActivationLayer readableclass0236$inner01321 = arraydeque.peekLast();
                        if (readableclass0236$inner01321 != null && flag && !this.method00280(readableclass0236$inner01321, readableclass0236$inner0132)) {
                           arraydeque.clear();
                        }

                        arraydeque.addLast(readableclass0236$inner0132);

                        while (arraydeque.size() > this.rotationModelManagerValueStart) {
                           arraydeque.removeFirst();
                        }

                        if (arraydeque.size() >= this.rotationModelManagerValueStart) {
                           ActivationLayer readableclass0236$inner01322 = arraydeque.peekLast();
                           if (readableclass0236$inner01322 != null) {
                              long l1 = flag && readableclass0236$inner01322.activationLayerLongValue != null
                                 ? readableclass0236$inner01322.activationLayerLongValue
                                 : k1;
                              if (!hashset.contains(l1)) {
                                 float[][] afloat1 = new float[this.rotationModelManagerValueStart][this.rotationModelManagerZenithRotationModel.rotationModelValue];
                                 int i2 = 0;

                                 for (ActivationLayer readableclass0236$inner01323 : arraydeque) {
                                    afloat1[i2++] = readableclass0236$inner01323.activationLayerAmountArray;
                                 }

                                 float[] afloat2 = this.method00280(afloat1);
                                 String s2 = "target=[n/a, n/a] err=[n/a, n/a]";
                                 if (readableclass0236$inner01322 != null
                                    && readableclass0236$inner01322.activationLayerFloatValue != null
                                    && readableclass0236$inner01322.activationLayerFloatValueSecondary != null) {
                                    float f2 = afloat2[0] - readableclass0236$inner01322.activationLayerFloatValue;
                                    float f3 = afloat2[1] - readableclass0236$inner01322.activationLayerFloatValueSecondary;
                                    s2 = String.format(
                                       Locale.ROOT,
                                       "target=[%.6f, %.6f] err=[%.6f, %.6f]",
                                       readableclass0236$inner01322.activationLayerFloatValue,
                                       readableclass0236$inner01322.activationLayerFloatValueSecondary,
                                       f2,
                                       f3
                                    );
                                 }

                                 if (j1 == 0) {
                                    this.method01162("[DeepLearning] Model-check dataset: " + file1.getPath());
                                    this.method01162(
                                       String.format(
                                          Locale.ROOT, "[DeepLearning] Model-check java_aligned_windows up to %d sessions (one window per session)", 15
                                       )
                                    );
                                 }

                                 if (flag && readableclass0236$inner01322 != null) {
                                    this.method01162(
                                       String.format(
                                          Locale.ROOT,
                                          "[DeepLearning] Model-check window end: sample=java_aligned_session_%d session=%d age=%d timestamp_ms=%d line=%d",
                                          j1 + 1,
                                          readableclass0236$inner01322.activationLayerLongValue,
                                          readableclass0236$inner01322.activationLayerIntegerValue,
                                          readableclass0236$inner01322.activationLayerLongValueSecondary,
                                          readableclass0236$inner01322.activationLayerValue
                                       )
                                    );
                                 }

                                 this.method01162(
                                    String.format(Locale.ROOT, "[DeepLearning] Model-check prediction: pred=[%.6f, %.6f] %s", afloat2[0], afloat2[1], s2)
                                 );
                                 hashset.add(l1);
                                 if (++j1 >= 15) {
                                    break;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            if (j1 > 0) {
               return true;
            }
         }

         this.method01162("[DeepLearning] Model-check skipped: no valid window in " + file1.getPath());
         return false;
      } else {
         this.method01162("[DeepLearning] Model-check skipped: model config.feature_columns missing or invalid.");
         return false;
      }
   }

   public boolean method00280(ActivationLayer readableclass0236$inner0132, ActivationLayer readableclass0236$inner01321) {
      if (readableclass0236$inner0132.activationLayerLongValue == null || readableclass0236$inner01321.activationLayerLongValue == null) {
         return true;
      } else if (!readableclass0236$inner0132.activationLayerLongValue.equals(readableclass0236$inner01321.activationLayerLongValue)) {
         return false;
      } else if (readableclass0236$inner01321.activationLayerIntegerValue == null
         || readableclass0236$inner0132.activationLayerIntegerValue == null
         || readableclass0236$inner01321.activationLayerIntegerValue < readableclass0236$inner0132.activationLayerIntegerValue) {
         return false;
      } else if (readableclass0236$inner01321.activationLayerIntegerValue - readableclass0236$inner0132.activationLayerIntegerValue > 3) {
         return false;
      } else {
         return readableclass0236$inner01321.activationLayerLongValueSecondary != null
               && readableclass0236$inner0132.activationLayerLongValueSecondary != null
               && readableclass0236$inner01321.activationLayerLongValueSecondary >= readableclass0236$inner0132.activationLayerLongValueSecondary
            ? readableclass0236$inner01321.activationLayerLongValueSecondary - readableclass0236$inner0132.activationLayerLongValueSecondary <= 1000L
            : false;
      }
   }

   public int[] method00280(String[] astring, long[] along) {
      int[] aint = new int[along.length];

      for (int i = 0; i < along.length; i++) {
         int j = this.method00280(astring, along[i]);
         if (j < 0) {
            throw new IllegalArgumentException("Dataset is missing model feature #" + (i + 1));
         }

         aint[i] = j;
      }

      return aint;
   }

   public int method00280(String[] astring, String[] astring1) {
      for (String s : astring1) {
         int i = this.method00280(astring, s);
         if (i >= 0) {
            return i;
         }
      }

      return -1;
   }

   public int method00280(String[] astring, String s) {
      for (int i = 0; i < astring.length; i++) {
         String s1 = this.method01171(astring[i]);
         if (s.equals(s1)) {
            return i;
         }
      }

      return -1;
   }

   public int method00280(String[] astring, long i) {
      for (int j = 0; j < astring.length; j++) {
         if (this.method01260(astring[j]) == i) {
            return j;
         }
      }

      return -1;
   }

   public String method01171(String s) {
      String s1 = s == null ? "" : s.trim();
      if (!s1.isEmpty() && s1.charAt(0) == '\ufeff') {
         s1 = s1.substring(1);
      }

      return s1;
   }

   public float[] method00280(String[] astring, int[] aint) {
      float[] afloat = new float[aint.length];

      for (int i = 0; i < aint.length; i++) {
         Float f = this.method00280(astring, aint[i]);
         if (f == null) {
            return null;
         }

         afloat[i] = f;
      }

      return afloat;
   }

   public Float method00280(String[] astring, int i) {
      if (i >= 0 && i < astring.length) {
         String s = astring[i].trim();
         if (s.isEmpty()) {
            return null;
         } else {
            try {
               float f = Float.parseFloat(s);
               return this.isFinite(f) ? f : null;
            } catch (NumberFormatException var5) {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   public Integer method00811(String[] astring, int i) {
      if (i >= 0 && i < astring.length) {
         String s = astring[i].trim();
         if (s.isEmpty()) {
            return null;
         } else {
            try {
               return Integer.parseInt(s);
            } catch (NumberFormatException var7) {
               try {
                  float f = Float.parseFloat(s);
                  return this.isFinite(f) ? (int)f : null;
               } catch (NumberFormatException var6) {
                  return null;
               }
            }
         }
      } else {
         return null;
      }
   }

   public Long method00643(String[] astring, int i) {
      if (i >= 0 && i < astring.length) {
         String s = astring[i].trim();
         if (s.isEmpty()) {
            return null;
         } else {
            try {
               return Long.parseLong(s);
            } catch (NumberFormatException var8) {
               try {
                  double d0 = Double.parseDouble(s);
                  return !Double.isNaN(d0) && !Double.isInfinite(d0) ? (long)d0 : null;
               } catch (NumberFormatException var7) {
                  return null;
               }
            }
         }
      } else {
         return null;
      }
   }

   public String[] method00620(String s) {
      ArrayList<String> arraylist = new ArrayList<>();
      StringBuilder stringbuilder = new StringBuilder();
      boolean flag = false;

      for (int i = 0; i < s.length(); i++) {
         char c0 = s.charAt(i);
         if (c0 == '"') {
            if (flag && i + 1 < s.length() && s.charAt(i + 1) == '"') {
               stringbuilder.append('"');
               i++;
            } else {
               flag = !flag;
            }
         } else if (c0 == ',' && !flag) {
            arraylist.add(stringbuilder.toString());
            stringbuilder.setLength(0);
         } else {
            stringbuilder.append(c0);
         }
      }

      arraylist.add(stringbuilder.toString());
      return arraylist.toArray(new String[0]);
   }

   public ZenithRotationModel method00167(JsonObject jsonobject) {
      ZenithRotationModel readableclass0236$inner0135 = new RotationModel();
      JsonObject jsonobject1 = jsonobject.getAsJsonObject("config");
      if (jsonobject1 == null) {
         throw new IllegalArgumentException("Model JSON is missing config section");
      } else {
         String s = jsonobject1.has("model_type") ? jsonobject1.get("model_type").getAsString() : "";
         if (!"direct_gru_moe_closed_loop".equalsIgnoreCase(s)) {
            throw new IllegalArgumentException(
               "Model format mismatch: expected config.model_type='direct_gru_moe_closed_loop' but got '" + s + "'. Re-train model."
            );
         } else {
            readableclass0236$inner0135.rotationModelText = "in_proj_in_norm_silu";
            readableclass0236$inner0135.rotationModelValue = jsonobject1.has("input_size") ? jsonobject1.get("input_size").getAsInt() : 28;
            readableclass0236$inner0135.rotationModelValueSecondary = jsonobject1.get("hidden_size").getAsInt();
            readableclass0236$inner0135.rotationModelValueTertiary = jsonobject1.get("num_layers").getAsInt();
            readableclass0236$inner0135.rotationModelValueAlternate = jsonobject1.has("fc_size") ? jsonobject1.get("fc_size").getAsInt() : -1;
            readableclass0236$inner0135.rotationModelValueCurrent = jsonobject1.has("output_size") ? jsonobject1.get("output_size").getAsInt() : 2;
            readableclass0236$inner0135.rotationModelValueNext = jsonobject1.has("moe_experts") ? jsonobject1.get("moe_experts").getAsInt() : 0;
            readableclass0236$inner0135.rotationModelEnabled = !jsonobject1.has("fixed_idle_expert") || jsonobject1.get("fixed_idle_expert").getAsBoolean();
            readableclass0236$inner0135.rotationModelEnabledSecondary = jsonobject1.has("two_head") && jsonobject1.get("two_head").getAsBoolean();
            readableclass0236$inner0135.rotationModelTimestampArray = jsonobject1.has("feature_columns")
               ? this.method00962(jsonobject1.getAsJsonArray("feature_columns"))
               : null;
            if (jsonobject1.has("sequence_length")) {
               this.rotationModelManagerValueStart = Math.max(2, jsonobject1.get("sequence_length").getAsInt());
            }

            if (readableclass0236$inner0135.rotationModelValue <= 0) {
               throw new IllegalArgumentException("Invalid input_size=" + readableclass0236$inner0135.rotationModelValue);
            } else if (readableclass0236$inner0135.rotationModelValueCurrent != 2) {
               throw new IllegalArgumentException("Expected output_size=2, got " + readableclass0236$inner0135.rotationModelValueCurrent);
            } else if (readableclass0236$inner0135.rotationModelValueNext < 2) {
               throw new IllegalArgumentException("Expected moe_experts>=2, got " + readableclass0236$inner0135.rotationModelValueNext);
            } else if (readableclass0236$inner0135.rotationModelTimestampArray != null
               && readableclass0236$inner0135.rotationModelTimestampArray.length != readableclass0236$inner0135.rotationModelValue) {
               throw new IllegalArgumentException("config.feature_columns size mismatch with input_size");
            } else {
               JsonObject jsonobject2 = jsonobject.getAsJsonObject("weights");
               if (jsonobject2 == null) {
                  throw new IllegalArgumentException("Model JSON is missing weights section");
               } else {
                  if (jsonobject2.has("feature_norm")) {
                     JsonObject jsonobject3 = jsonobject2.getAsJsonObject("feature_norm");
                     readableclass0236$inner0135.rotationModelDenseLayer = new DenseLayer();
                     readableclass0236$inner0135.rotationModelDenseLayer.denseLayerAmountArray = this.method00811(jsonobject3.getAsJsonArray("mean"));
                     readableclass0236$inner0135.rotationModelDenseLayer.denseLayerAmountArraySecondary = this.method00811(jsonobject3.getAsJsonArray("std"));
                     if (readableclass0236$inner0135.rotationModelDenseLayer.denseLayerAmountArray.length != readableclass0236$inner0135.rotationModelValue
                        || readableclass0236$inner0135.rotationModelDenseLayer.denseLayerAmountArraySecondary.length
                           != readableclass0236$inner0135.rotationModelValue) {
                        throw new IllegalArgumentException("feature_norm size mismatch with input_size");
                     }
                  } else {
                     readableclass0236$inner0135.rotationModelDenseLayer = new DenseLayer();
                     readableclass0236$inner0135.rotationModelDenseLayer.denseLayerAmountArray = new float[readableclass0236$inner0135.rotationModelValue];
                     readableclass0236$inner0135.rotationModelDenseLayer.denseLayerAmountArraySecondary = new float[readableclass0236$inner0135.rotationModelValue];

                     for (int j = 0; j < readableclass0236$inner0135.rotationModelValue; j++) {
                        readableclass0236$inner0135.rotationModelDenseLayer.denseLayerAmountArray[j] = 0.0F;
                        readableclass0236$inner0135.rotationModelDenseLayer.denseLayerAmountArraySecondary[j] = 1.0F;
                     }
                  }

                  JsonObject jsonobject5 = jsonobject2.getAsJsonObject("in_proj");
                  JsonObject jsonobject4 = jsonobject2.getAsJsonObject("in_norm");
                  if (jsonobject5 != null && jsonobject4 != null) {
                     readableclass0236$inner0135.rotationModelAmountArray = this.method00643(jsonobject5.getAsJsonArray("weight"));
                     readableclass0236$inner0135.rotationModelAmountArraySecondary = this.method00811(jsonobject5.getAsJsonArray("bias"));
                     readableclass0236$inner0135.rotationModelNormalizationLayer = new NormalizationLayer();
                     readableclass0236$inner0135.rotationModelNormalizationLayer.normalizationLayerAmountArray = this.method00811(
                        jsonobject4.getAsJsonArray("weight")
                     );
                     readableclass0236$inner0135.rotationModelNormalizationLayer.normalizationLayerAmountArraySecondary = this.method00811(
                        jsonobject4.getAsJsonArray("bias")
                     );
                     if (readableclass0236$inner0135.rotationModelAmountArray.length == readableclass0236$inner0135.rotationModelValueSecondary
                        && readableclass0236$inner0135.rotationModelAmountArraySecondary.length == readableclass0236$inner0135.rotationModelValueSecondary) {
                        for (float[] afloat : readableclass0236$inner0135.rotationModelAmountArray) {
                           if (afloat.length != readableclass0236$inner0135.rotationModelValue) {
                              throw new IllegalArgumentException("weights.in_proj inner size mismatch with input_size");
                           }
                        }

                        if (readableclass0236$inner0135.rotationModelNormalizationLayer.normalizationLayerAmountArray.length
                              == readableclass0236$inner0135.rotationModelValueSecondary
                           && readableclass0236$inner0135.rotationModelNormalizationLayer.normalizationLayerAmountArraySecondary.length
                              == readableclass0236$inner0135.rotationModelValueSecondary) {
                           readableclass0236$inner0135.rotationModelValuePrevious = readableclass0236$inner0135.rotationModelValueSecondary;
                           JsonObject jsonobject6 = jsonobject2.getAsJsonObject("gru");
                           if (jsonobject6 == null) {
                              throw new IllegalArgumentException("Model JSON is missing weights.gru");
                           } else {
                              readableclass0236$inner0135.rotationModelOutputLayerArray = new OutputLayer[readableclass0236$inner0135.rotationModelValueTertiary];

                              for (int k = 0; k < readableclass0236$inner0135.rotationModelValueTertiary; k++) {
                                 JsonObject jsonobject8 = jsonobject6.getAsJsonObject("l" + k);
                                 if (jsonobject8 == null) {
                                    throw new IllegalArgumentException("Missing GRU layer: l" + k);
                                 }

                                 OutputLayer readableclass0236$inner0134 = new OutputLayer();
                                 readableclass0236$inner0134.outputLayerAmountArray = this.method00643(jsonobject8.getAsJsonArray("weight_ih"));
                                 readableclass0236$inner0134.outputLayerAmountArraySecondary = this.method00643(jsonobject8.getAsJsonArray("weight_hh"));
                                 readableclass0236$inner0134.outputLayerAmountArrayTertiary = this.method00811(jsonobject8.getAsJsonArray("bias_ih"));
                                 readableclass0236$inner0134.outputLayerAmountArrayAlternate = this.method00811(jsonobject8.getAsJsonArray("bias_hh"));
                                 int i = k == 0
                                    ? readableclass0236$inner0135.rotationModelValuePrevious
                                    : readableclass0236$inner0135.rotationModelValueSecondary;
                                 if (readableclass0236$inner0134.outputLayerAmountArray.length != 3 * readableclass0236$inner0135.rotationModelValueSecondary) {
                                    throw new IllegalArgumentException("GRU weight_ih rows mismatch at layer l" + k);
                                 }

                                 if (readableclass0236$inner0134.outputLayerAmountArraySecondary.length
                                    != 3 * readableclass0236$inner0135.rotationModelValueSecondary) {
                                    throw new IllegalArgumentException("GRU weight_hh rows mismatch at layer l" + k);
                                 }

                                 if (readableclass0236$inner0134.outputLayerAmountArrayTertiary.length
                                       != 3 * readableclass0236$inner0135.rotationModelValueSecondary
                                    || readableclass0236$inner0134.outputLayerAmountArrayAlternate.length
                                       != 3 * readableclass0236$inner0135.rotationModelValueSecondary) {
                                    throw new IllegalArgumentException("GRU bias size mismatch at layer l" + k);
                                 }

                                 for (float[] afloat1 : readableclass0236$inner0134.outputLayerAmountArray) {
                                    if (afloat1.length != i) {
                                       throw new IllegalArgumentException("GRU weight_ih input size mismatch at layer l" + k);
                                    }
                                 }

                                 for (float[] afloat4 : readableclass0236$inner0134.outputLayerAmountArraySecondary) {
                                    if (afloat4.length != readableclass0236$inner0135.rotationModelValueSecondary) {
                                       throw new IllegalArgumentException("GRU weight_hh hidden size mismatch at layer l" + k);
                                    }
                                 }

                                 readableclass0236$inner0135.rotationModelOutputLayerArray[k] = readableclass0236$inner0134;
                              }

                              JsonObject jsonobject7 = jsonobject2.getAsJsonObject("fc1");
                              if (jsonobject7 == null) {
                                 throw new IllegalArgumentException("Model JSON is missing weights.fc1");
                              } else {
                                 readableclass0236$inner0135.rotationModelAmountArrayTertiary = this.method00643(jsonobject7.getAsJsonArray("weight"));
                                 readableclass0236$inner0135.rotationModelAmountArrayAlternate = this.method00811(jsonobject7.getAsJsonArray("bias"));
                                 JsonObject jsonobject9 = jsonobject2.getAsJsonObject("fc2");
                                 if (jsonobject9 == null) {
                                    throw new IllegalArgumentException("Model JSON is missing weights.fc2");
                                 } else {
                                    readableclass0236$inner0135.rotationModelAmountArrayPrevious = this.method00643(jsonobject9.getAsJsonArray("weight"));
                                    readableclass0236$inner0135.rotationModelAmountArrayCurrent = this.method00811(jsonobject9.getAsJsonArray("bias"));
                                    if (readableclass0236$inner0135.rotationModelAmountArrayTertiary.length != 0
                                       && readableclass0236$inner0135.rotationModelAmountArrayAlternate.length != 0
                                       && readableclass0236$inner0135.rotationModelAmountArrayPrevious.length != 0
                                       && readableclass0236$inner0135.rotationModelAmountArrayCurrent.length != 0) {
                                       for (float[] afloat2 : readableclass0236$inner0135.rotationModelAmountArrayTertiary) {
                                          if (afloat2.length != readableclass0236$inner0135.rotationModelValueSecondary) {
                                             throw new IllegalArgumentException("weights.fc1 input size mismatch with hidden_size");
                                          }
                                       }

                                       if (readableclass0236$inner0135.rotationModelAmountArrayTertiary.length
                                          != readableclass0236$inner0135.rotationModelAmountArrayAlternate.length) {
                                          throw new IllegalArgumentException("weights.fc1 rows must match fc1 bias length");
                                       } else {
                                          int l = readableclass0236$inner0135.rotationModelAmountArrayTertiary.length;
                                          if (readableclass0236$inner0135.rotationModelValueAlternate <= 0) {
                                             readableclass0236$inner0135.rotationModelValueAlternate = l;
                                          } else if (readableclass0236$inner0135.rotationModelValueAlternate != l) {
                                             throw new IllegalArgumentException("config.fc_size mismatch with weights.fc1");
                                          }

                                          if (readableclass0236$inner0135.rotationModelAmountArrayPrevious.length
                                                == readableclass0236$inner0135.rotationModelValueAlternate
                                             && readableclass0236$inner0135.rotationModelAmountArrayCurrent.length
                                                == readableclass0236$inner0135.rotationModelValueAlternate) {
                                             for (float[] afloat3 : readableclass0236$inner0135.rotationModelAmountArrayPrevious) {
                                                if (afloat3.length != readableclass0236$inner0135.rotationModelValueAlternate) {
                                                   throw new IllegalArgumentException("weights.fc2 input size mismatch with fc_size");
                                                }
                                             }

                                             boolean flag = jsonobject2.has("gate_yaw") && jsonobject2.has("gate_pitch");
                                             boolean flag1 = jsonobject2.has("move_yaw") && jsonobject2.has("move_pitch");
                                             readableclass0236$inner0135.rotationModelEnabledTertiary = flag;
                                             readableclass0236$inner0135.rotationModelEnabledSecondary = readableclass0236$inner0135.rotationModelEnabledSecondary
                                                || flag1;
                                             if (flag) {
                                                readableclass0236$inner0135.rotationModelAmountArrayEnd = this.method00643(
                                                   jsonobject2.getAsJsonObject("gate_yaw").getAsJsonArray("weight")
                                                );
                                                readableclass0236$inner0135.rotationModelAmountArrayVariantA = this.method00811(
                                                   jsonobject2.getAsJsonObject("gate_yaw").getAsJsonArray("bias")
                                                );
                                                readableclass0236$inner0135.rotationModelAmountArrayVariantB = this.method00643(
                                                   jsonobject2.getAsJsonObject("gate_pitch").getAsJsonArray("weight")
                                                );
                                                readableclass0236$inner0135.rotationModelAmountArrayVariantC = this.method00811(
                                                   jsonobject2.getAsJsonObject("gate_pitch").getAsJsonArray("bias")
                                                );
                                                readableclass0236$inner0135.rotationModelAmountArrayVariantD = this.method00643(
                                                   jsonobject2.getAsJsonObject("experts_yaw").getAsJsonArray("weight")
                                                );
                                                readableclass0236$inner0135.rotationModelAmountArrayVariantE = this.method00811(
                                                   jsonobject2.getAsJsonObject("experts_yaw").getAsJsonArray("bias")
                                                );
                                                readableclass0236$inner0135.rotationModelAmountArrayVariantF = this.method00643(
                                                   jsonobject2.getAsJsonObject("experts_pitch").getAsJsonArray("weight")
                                                );
                                                readableclass0236$inner0135.rotationModelAmountArrayVariantG = this.method00811(
                                                   jsonobject2.getAsJsonObject("experts_pitch").getAsJsonArray("bias")
                                                );
                                                if (readableclass0236$inner0135.rotationModelEnabledSecondary) {
                                                   if (!flag1) {
                                                      throw new IllegalArgumentException(
                                                         "Model JSON config.two_head=true but weights.move_yaw/move_pitch are missing"
                                                      );
                                                   }

                                                   readableclass0236$inner0135.rotationModelAmountArrayVariantH = this.method00643(
                                                      jsonobject2.getAsJsonObject("move_yaw").getAsJsonArray("weight")
                                                   );
                                                   readableclass0236$inner0135.rotationModelAmountArrayVariantI = this.method00811(
                                                      jsonobject2.getAsJsonObject("move_yaw").getAsJsonArray("bias")
                                                   );
                                                   readableclass0236$inner0135.rotationModelAmountArrayVariantJ = this.method00643(
                                                      jsonobject2.getAsJsonObject("move_pitch").getAsJsonArray("weight")
                                                   );
                                                   readableclass0236$inner0135.rotationModelAmountArrayVariantK = this.method00811(
                                                      jsonobject2.getAsJsonObject("move_pitch").getAsJsonArray("bias")
                                                   );
                                                   this.method00280(
                                                      readableclass0236$inner0135.rotationModelAmountArrayVariantH,
                                                      readableclass0236$inner0135.rotationModelAmountArrayVariantI,
                                                      "move_yaw",
                                                      readableclass0236$inner0135.rotationModelValueAlternate
                                                   );
                                                   this.method00280(
                                                      readableclass0236$inner0135.rotationModelAmountArrayVariantJ,
                                                      readableclass0236$inner0135.rotationModelAmountArrayVariantK,
                                                      "move_pitch",
                                                      readableclass0236$inner0135.rotationModelValueAlternate
                                                   );
                                                }
                                             } else {
                                                if (readableclass0236$inner0135.rotationModelEnabledSecondary) {
                                                   throw new IllegalArgumentException("Two-head models require split yaw/pitch gate and expert heads");
                                                }

                                                JsonObject jsonobject10 = jsonobject2.getAsJsonObject("gate");
                                                if (jsonobject10 == null) {
                                                   throw new IllegalArgumentException("Model JSON is missing weights.gate");
                                                }

                                                readableclass0236$inner0135.rotationModelAmountArrayNext = this.method00643(
                                                   jsonobject10.getAsJsonArray("weight")
                                                );
                                                readableclass0236$inner0135.rotationModelAmountArrayMinimum = this.method00811(
                                                   jsonobject10.getAsJsonArray("bias")
                                                );
                                                if (readableclass0236$inner0135.rotationModelAmountArrayNext.length
                                                      != readableclass0236$inner0135.rotationModelValueNext
                                                   || readableclass0236$inner0135.rotationModelAmountArrayMinimum.length
                                                      != readableclass0236$inner0135.rotationModelValueNext) {
                                                   throw new IllegalArgumentException("weights.gate size mismatch with moe_experts");
                                                }

                                                JsonObject jsonobject11 = jsonobject2.getAsJsonObject("experts");
                                                if (jsonobject11 == null) {
                                                   throw new IllegalArgumentException("Model JSON is missing weights.experts");
                                                }

                                                readableclass0236$inner0135.rotationModelAmountArrayMaximum = this.method00643(
                                                   jsonobject11.getAsJsonArray("weight")
                                                );
                                                readableclass0236$inner0135.rotationModelAmountArrayStart = this.method00811(
                                                   jsonobject11.getAsJsonArray("bias")
                                                );
                                                int i1 = (
                                                      readableclass0236$inner0135.rotationModelEnabled
                                                         ? readableclass0236$inner0135.rotationModelValueNext - 1
                                                         : readableclass0236$inner0135.rotationModelValueNext
                                                   )
                                                   * readableclass0236$inner0135.rotationModelValueCurrent;
                                                if (readableclass0236$inner0135.rotationModelAmountArrayMaximum.length != i1
                                                   || readableclass0236$inner0135.rotationModelAmountArrayStart.length != i1) {
                                                   throw new IllegalArgumentException("weights.experts size mismatch with moe_experts/fixed_idle_expert");
                                                }
                                             }

                                             return readableclass0236$inner0135;
                                          } else {
                                             throw new IllegalArgumentException("weights.fc2 size mismatch with fc_size");
                                          }
                                       }
                                    } else {
                                       throw new IllegalArgumentException("weights.fc1 / weights.fc2 cannot be empty");
                                    }
                                 }
                              }
                           }
                        } else {
                           throw new IllegalArgumentException("weights.in_norm size mismatch with hidden_size");
                        }
                     } else {
                        throw new IllegalArgumentException("weights.in_proj size mismatch with hidden_size");
                     }
                  } else {
                     throw new IllegalArgumentException("Model JSON is missing weights.in_proj or weights.in_norm");
                  }
               }
            }
         }
      }
   }

   public float[] method00811(JsonArray jsonarray) {
      float[] afloat = new float[jsonarray.size()];

      for (int i = 0; i < jsonarray.size(); i++) {
         afloat[i] = jsonarray.get(i).getAsFloat();
      }

      return afloat;
   }

   public float[][] method00643(JsonArray jsonarray) {
      float[][] afloat = new float[jsonarray.size()][];

      for (int i = 0; i < jsonarray.size(); i++) {
         afloat[i] = this.method00811(jsonarray.get(i).getAsJsonArray());
      }

      return afloat;
   }

   public void method00280(float[][] afloat, float[] afloat1, String s, int i) {
      if (afloat == null || afloat1 == null || afloat.length != 1 || afloat1.length != 1) {
         throw new IllegalArgumentException("weights." + s + " must be a single-output linear head");
      } else if (afloat[0].length != i) {
         throw new IllegalArgumentException("weights." + s + " input size mismatch with fc_size");
      }
   }

   public long[] method00962(JsonArray jsonarray) {
      long[] along = new long[jsonarray.size()];

      for (int i = 0; i < jsonarray.size(); i++) {
         String s = jsonarray.get(i).getAsString();
         along[i] = this.method00623(s) ? Long.parseUnsignedLong(s.substring("fc_".length()), 16) : this.method01260(s);
      }

      return along;
   }

   public boolean method01297() {
      return this.rotationModelManagerEnabled;
   }

   public ZenithRotationModel method01027() {
      return this.rotationModelManagerRotationModel;
   }

   public int method00344() {
      return this.rotationModelManagerValueStart;
   }

   public Deque<float[]> method00845() {
      return this.rotationModelManagerItemsSecondary;
   }

   public Random method00367() {
      return this.rotationModelManagerRandom;
   }

   public int method01163() {
      return this.rotationModelManagerValueEnd;
   }

   public int method00276() {
      return this.rotationModelManagerValueVariantA;
   }

   public int method00027() {
      return this.rotationModelManagerValueVariantB;
   }

   public Deque<float[]> method00058() {
      return this.rotationModelManagerItems;
   }

    public void loadModel() { method00800(); }
    public boolean isLoaded() { return method01297(); }
    public ZenithRotationModel getModel() { return method01027(); }
    public int getSequenceLength() { return method00344(); }
    public int getInputSize() { return method00395(); }
    public void resetBuffer() { method00423(); }
    public ZenithRotationPrediction predict(float[] features) { return method00811(features); }
    public float[] predictArray(float[] features) { return method00280(features); }
}

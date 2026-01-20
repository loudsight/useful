package com.loudsight.utilities.io;

import com.loudsight.useful.helper.logging.LoggingHelper;

import java.lang.invoke.MethodHandles;
import java.util.Map;

class JarUtils {
    private static final LoggingHelper logger = LoggingHelper.wrap(MethodHandles.lookup().lookupClass());

    public void decompressJarFile(Map<String, String> srcToDestMap) {
        try {
//            val f = File(this::class.java
//                    .protectionDomain
//                    .codeSource
//                    .location
//                    .toURI()
//                    .path)
//            var dest = f.absolutePath + "/../../"

//            File jarFile = new File(com.loudsight.cloud.client.CloudClientApi.class
//                    .getProtectionDomain()
//                    .getCodeSource()
//                    .getLocation()
//                    .toURI()
//                    .getPath())
//            String src = jarFile.getAbsolutePath()
//            dest = File(dest).absoluteFile.absolutePath
//            val BUFFER = 2048
//            FileInputStream fis = new FileInputStream(src)
//            CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32())
//            JarInputStream zis = new JarInputStream(new BufferedInputStream(checksum))
//            ZipEntry entry
//
//            while ((entry = zis.getNextEntry()) != null) {
//
//                String target
//                String[] toks = entry.getName().split("/", 2)
//                if ((target = srcToDestMap.get(toks[0])) == null)
//                    continue
//
//                String destFileName = dest + target + toks[1]
//                if (entry.isDirectory()) {
//                    new File(destFileName).mkdirs()
//                    continue
//                }
//                int count
//                byte data[] = new byte[BUFFER]
//                File destFile = new File(destFileName)
//
//                if (destFile.exists()) {
//                    destFile.delete()
//                }
//                FileOutputStream fos = new FileOutputStream(destFileName)
//                BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER)
//
//                while ((count = zis.read(data, 0, BUFFER)) != -1) {
//                    bos.write(data, 0, count)
//                }
//                bos.flush()
//                bos.close()
//            }
//            zis.close()
        } catch (Exception e) {
            logger.logError("Unexpected error", e);
        }
    }

}
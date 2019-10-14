package example;

import java.io.*;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.StreamRequestHandler;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GetObjectRequest;

/**
 * Hello world!
 *
 */
public class App implements StreamRequestHandler
{
    public static void main( String[] args )

    {
        System.out.println( "Hello World!" );
    }

    @Override
    public void handleRequest(
            InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        OSSClient client = new OSSClient(
                "http://oss-cn-shanghai.aliyuncs.com",
                context.getExecutionCredentials().getAccessKeyId(),
                context.getExecutionCredentials().getAccessKeySecret(),
                context.getExecutionCredentials().getSecurityToken());

        String bucketName = "sunfeiyu";
        String objectName = "qq-v2.apk";
        String outObjectName = "qq-v2-signed.apk";
        String inputApk = "/tmp/input.apk";
        String outputApk = "/tmp/output.apk";

        // 1. download original apk
        client.getObject(new GetObjectRequest(bucketName, objectName), new File(inputApk));

        // 2. adding channel info
        String cmd = "java -jar /code/walle-cli-all.jar put -c aliyun-fc";
        cmd += " " + inputApk;
        cmd += " " + outputApk;

        context.getLogger().info("cmd: " + cmd);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", cmd);

        try {
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                context.getLogger().info("Success!");
                outputStream.write("Success".getBytes());
            } else {
                //abnormal...
                context.getLogger().error("Failed!");
                context.getLogger().error("status: " + exitVal);
                outputStream.write("Failed".getBytes());
            }
            System.out.println(output);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3. upload new apk
        client.putObject(bucketName, outObjectName, new File(outputApk));

        client.shutdown();
    }
}
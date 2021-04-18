package service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Scanner;

@Component
@Log4j2
public class FileService {

    @Value("#{errors_file['local_file']}")
    private String errorsFile;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    //    @Value("#{errors_file['linux_file']}")
//    private String errorsFile;
    public void writeError(String s) {
        try {
            File file = new File(errorsFile + "/" + format.format(new Date()) + ".txt");
            FileWriter fw = new FileWriter(
                    file, true);

            fw.write("(error) " + LocalDateTime.now() + " : " + s + "\n");
            fw.flush();
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
}

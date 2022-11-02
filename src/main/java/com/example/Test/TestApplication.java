package com.example.Test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

@SpringBootApplication
public class TestApplication implements ApplicationRunner  {


	private String name = "Comment";

	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}

	@Override
	public void run( ApplicationArguments args ) throws Exception
	{
		String repository = "package com.example.Test;\n" +
				"\n" +
				"import org.springframework.data.jpa.repository.JpaRepository;\n" +
				"import org.springframework.stereotype.Repository;\n" +
				"\n" +
				"@Repository\n" +
				"public interface "+name+"Repository extends JpaRepository<"+name+", Long> {\n" +
				"}\n" ;

		String controller = "package com.example.Test;\n" +
				"\n" +
				"import org.springframework.beans.factory.annotation.Autowired;\n" +
				"import org.springframework.http.ResponseEntity;\n" +
				"import org.springframework.stereotype.Controller;\n" +
				"import org.springframework.web.bind.annotation.DeleteMapping;\n" +
				"import org.springframework.web.bind.annotation.GetMapping;\n" +
				"\n" +
				"import java.util.List;\n" +
				"\n" +
				"@Controller\n" +
				"public class "+name+"Controller {\n" +
				"\n" +
				"    @Autowired\n" +
				"    private "+name+"Repository "+name+"Repository;\n" +
				"\n" +
				"    @GetMapping(\"/"+name+"s\")\n" +
				"    public ResponseEntity getAll"+name+"(){\n" +
				"        List<"+name+"> list"+name+" = "+name+"Repository.findAll();\n" +
				"        return ResponseEntity.ok(list"+name+");\n" +
				"    }\n" +
				"\n" +
				"    @DeleteMapping(\"/"+name+"s\")\n" +
				"    public ResponseEntity<String> delete(){\n" +
				"        "+name+"Repository.deleteAll();\n" +
				"        return ResponseEntity.ok(\"Delete all\");\n" +
				"    }\n" +
				"}\n";
		Path myRepository = Paths.get("src/main/java/com/example/Test/CommentRepository.java");
		Path myController = Paths.get("src/main/java/com/example/Test/CommentController.java");

		Files.write(myRepository, repository.getBytes(StandardCharsets.UTF_8));
		Files.write(myController, controller.getBytes(StandardCharsets.UTF_8));
	}

}

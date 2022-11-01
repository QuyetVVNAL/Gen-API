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

	@Value("${person.name}")
	private String name;

	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
		System.out.println("Testaaaaaaaaaa");
	}

	@Override
	public void run( ApplicationArguments args ) throws Exception
	{
		String content = "test123";
		Path myPath = Paths.get("src/main/java/com/example/Test/Comment.java");

		Files.write(myPath, content.getBytes(StandardCharsets.UTF_8));
	}

}

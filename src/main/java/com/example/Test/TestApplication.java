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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class TestApplication implements ApplicationRunner  {

	@Value("${entity.name}")
	private String name;

	@Value("${path}")
	private String path;

	@Value("${create}")
	private Boolean create;

	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}

	@Override
	public void run( ApplicationArguments args ) throws Exception
	{
		String catalog = "test";
		String schemaPattern = "test";
		List<String> tableList = new ArrayList<>();
		try (Connection connection = DriverManager
				.getConnection("jdbc:mysql://localhost:3306/test?useSSL=false", "root", "!Ngaythu3")) {
			DatabaseMetaData databaseMetaData = connection.getMetaData();

			String userName = databaseMetaData.getUserName();
			//TODO: get Schemas
//			try(ResultSet schemas = databaseMetaData.getSchemas()){
//				while (schemas.next()){
//					String table_schem = schemas.getString("TABLE_SCHEM");
//					String table_catalog = schemas.getString("TABLE_CATALOG");
//					System.out.println(table_catalog + "catalog");
//				}
//			}

			try(ResultSet resultSet = databaseMetaData.getTables(catalog, schemaPattern, null,  new String[]{"TABLE"})){
				while(resultSet.next()) {
					tableList.add(resultSet.getString("TABLE_NAME"));
				}
			}
			System.out.println(tableList );
			for (String tableName: tableList) {
				try(ResultSet columns = databaseMetaData.getColumns(catalog,schemaPattern, tableName, null)){
					while(columns.next()) {
						String columnName = columns.getString("COLUMN_NAME");
						String columnSize = columns.getString("COLUMN_SIZE");
						String datatype = columns.getString("DATA_TYPE");
						String isNullable = columns.getString("IS_NULLABLE");
						String isAutoIncrement = columns.getString("IS_AUTOINCREMENT");
						System.out.println(columnName + " " + columnSize + " " + datatype + " " + isNullable + " " + isAutoIncrement + " ");
					}
				}

				try(ResultSet primaryKeys = databaseMetaData.getPrimaryKeys(catalog, schemaPattern, tableName)){
					while(primaryKeys.next()){
						String primaryKeyColumnName = primaryKeys.getString("COLUMN_NAME");
						String primaryKeyName = primaryKeys.getString("PK_NAME");
						System.out.println(primaryKeyName + " " + primaryKeyColumnName);
					}
				}
			}




		} catch (SQLException e) {
			System.out.println(e);
		}
		// mvn spring-boot:run -Dspring-boot.run.arguments="--entity.name=Comment --create=true --path=com.example.Test"
		if(create){
			String repository = "package "+path+".repository;\n" +
					"\n" +
					"import "+path+".entity."+name+";\n" +
					"import org.springframework.data.jpa.repository.JpaRepository;\n" +
					"import org.springframework.stereotype.Repository;\n" +
					"\n" +
					"@Repository\n" +
					"public interface "+name+"Repository extends JpaRepository<"+name+", Long> {\n" +
					"}" ;

			String serviceImpl ="package "+path+".service;\n" +
					"\n" +
					"import "+path+".entity."+name+";\n" +
					"import "+path+".repository."+name+"Repository;\n" +
					"import org.springframework.beans.factory.annotation.Autowired;\n" +
					"import org.springframework.stereotype.Component;\n" +
					"\n" +
					"import java.util.List;\n" +
					"\n" +
					"@Component\n" +
					"public class "+name+"ServiceImpl implements "+name+"Service {\n" +
					"    @Autowired\n" +
					"    private "+name+"Repository "+name+"Repository;\n" +
					"    @Override\n" +
					"    public List<"+name+"> findAll() {\n" +
					"        return "+name+"Repository.findAll();\n" +
					"    }\n" +
					"\n" +
					"    @Override\n" +
					"    public void deleteAll() {\n" +
					"        "+name+"Repository.deleteAll();\n" +
					"    }\n" +
					"}\n";

			String service ="package "+path+".service;\n" +
					"\n" +
					"import "+path+".entity."+name+";\n" +
					"import org.springframework.stereotype.Service;\n" +
					"\n" +
					"import java.util.List;\n" +
					"\n" +
					"@Service\n" +
					"public interface "+name+"Service {\n" +
					"    \n" +
					"    List<"+name+"> findAll();\n" +
					"    \n" +
					"    void deleteAll();\n" +
					"}\n";

			String controller = "package "+path+".controller;\n" +
					"\n" +
					"import "+path+".entity."+name+";\n" +
					"import "+path+".service."+name+"Service;\n" +
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
					"    private "+name+"Service "+name+"Service;\n" +
					"\n" +
					"    @GetMapping(\"/"+name+"s\")\n" +
					"    public ResponseEntity getAll"+name+"(){\n" +
					"        List<"+name+"> list"+name+" = "+name+"Service.findAll();\n" +
					"        return ResponseEntity.ok(list"+name+");\n" +
					"    }\n" +
					"\n" +
					"    @DeleteMapping(\"/"+name+"s\")\n" +
					"    public ResponseEntity<String> delete(){\n" +
					"        "+name+"Service.deleteAll();\n" +
					"        return ResponseEntity.ok(\"Delete all\");\n" +
					"    }\n" +
					"}\n";
			path = path.replace(".", "/");
			Path repositoryPath = Paths.get("src/main/java/"+path+"/repository/"+name+"Repository.java");
			Path serviceImplPath = Paths.get("src/main/java/"+path+"/service/"+name+"ServiceImpl.java");
			Path servicePath = Paths.get("src/main/java/"+path+"/service/"+name+"Service.java");
			Path controllerPath = Paths.get("src/main/java/"+path+"/controller/"+name+"Controller.java");

			Files.write(repositoryPath, repository.getBytes(StandardCharsets.UTF_8));
			System.out.println("create" + "src/main/java/"+path+"/repository/"+name+"Repository.java");
			Files.write(serviceImplPath, serviceImpl.getBytes(StandardCharsets.UTF_8));
			System.out.println("create" + "src/main/java/"+path+"/service/"+name+"ServiceImpl.java");
			Files.write(servicePath, service.getBytes(StandardCharsets.UTF_8));
			System.out.println("create" + "src/main/java/"+path+"/service/"+name+"Service.java");
			Files.write(controllerPath, controller.getBytes(StandardCharsets.UTF_8));
			System.out.println("create" + "src/main/java/"+path+"/controller/"+name+"Controller.java");
		}else {
			System.out.println("No generate API");
		}

	}

}

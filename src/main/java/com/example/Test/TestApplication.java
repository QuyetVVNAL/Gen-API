package com.example.Test;

import com.example.Test.entity.Column;
import com.example.Test.entity.TableInfo;
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
		List<String> tableListName = new ArrayList<>();
		List<TableInfo> tableInfos = new ArrayList<>();
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
					tableListName.add(resultSet.getString("TABLE_NAME").substring(0,1).toUpperCase() + resultSet.getString(1));
				}
			}
			for (String tableName: tableListName) {
				List<Column> columnList = new ArrayList<>();
				TableInfo table = new TableInfo();
				try(ResultSet columns = databaseMetaData.getColumns(catalog,schemaPattern, tableName, null)){
					while(columns.next()) {
						Column col = new Column();
						String columnName = columns.getString("COLUMN_NAME");
						col.setName(columnName);
						String columnSize = columns.getString("COLUMN_SIZE");
						col.setSize(columnSize);
						String datatype = columns.getString("DATA_TYPE");
						col.setType(datatype);
						String isNullable = columns.getString("IS_NULLABLE");
						col.setIsNullAble(isNullable);
						String isAutoIncrement = columns.getString("IS_AUTOINCREMENT");
						col.setIsAutoIncrement(isAutoIncrement);
						try(ResultSet primaryKeys = databaseMetaData.getPrimaryKeys(catalog, schemaPattern, tableName)){
							while(primaryKeys.next()){
								String primaryKeyColumnName = primaryKeys.getString("COLUMN_NAME");
								if (columnName.equals(primaryKeyColumnName)){
									String primaryKeyName = primaryKeys.getString("PK_NAME");
									col.setPrimaryKey(primaryKeyName);
								}

							}
						}
						columnList.add(col);
					}
				}

				table.setTableName(tableName);
				table.setColumns(columnList);
				tableInfos.add(table);
			}

		} catch (SQLException e) {
			System.out.println(e);
		}
		// mvn spring-boot:run -Dspring-boot.run.arguments="--entity.name=Comment --create=true --path=com.example.Test"
		if(create){
			for (TableInfo table: tableInfos) {
				String column ="";
				for (Column col: table.getColumns() ) {
					if(col.getPrimaryKey() != null){
						column += "    @Id\n" +
								"    @GeneratedValue(strategy = GenerationType.AUTO)\n" +
								"    private long id; \n";
					}else {
						column += "    @Column(name = \""+col.getName()+"\", nullable = true)\n" +
								"    private long stt;\n";
					}
				}
				String entity ="package "+path+".entity;\n" +
						"\n" +
						"import lombok.AllArgsConstructor;\n" +
						"import lombok.Getter;\n" +
						"import lombok.NoArgsConstructor;\n" +
						"import lombok.Setter;\n" +
						"\n" +
						"import javax.persistence.*;\n" +
						"import javax.persistence.Column;\n" +
						"\n" +
						"@Entity(name = \""+table.getTableName()+"\")\n" +
						"@Table(name = \""+table.getTableName()+"\")\n" +
						"@Setter\n" +
						"@Getter\n" +
						"@AllArgsConstructor\n" +
						"@NoArgsConstructor\n" +
						"public class "+table.getTableName()
						+" {\n" + column + "}\n";
				String repository = "package "+path+".repository;\n" +
						"\n" +
						"import org.springframework.data.jpa.repository.JpaRepository;\n" +
						"import org.springframework.stereotype.Repository;\n" +
						"\n" +
						"@Repository\n" +
						"public interface "+table.getTableName()+"Repository extends JpaRepository<"+table.getTableName()+", Long> {\n" +
						"}" ;

				String serviceImpl ="package "+path+".service;\n" +
						"\n" +
						"import "+path+".entity."+table.getTableName()+";\n" +
						"import "+path+".repository."+table.getTableName()+"Repository;\n" +
						"import org.springframework.beans.factory.annotation.Autowired;\n" +
						"import org.springframework.stereotype.Component;\n" +
						"\n" +
						"import java.util.List;\n" +
						"\n" +
						"@Component\n" +
						"public class "+table.getTableName()+"ServiceImpl implements "+table.getTableName()+"Service {\n" +
						"    @Autowired\n" +
						"    private "+table.getTableName()+"Repository "+table.getTableName()+"Repository;\n" +
						"    @Override\n" +
						"    public List<"+table.getTableName()+"> findAll() {\n" +
						"        return "+table.getTableName()+"Repository.findAll();\n" +
						"    }\n" +
						"\n" +
						"    @Override\n" +
						"    public void deleteAll() {\n" +
						"        "+table.getTableName()+"Repository.deleteAll();\n" +
						"    }\n" +
						"}\n";

				String service ="package "+path+".service;\n" +
						"\n" +
						"import "+path+".entity."+table.getTableName()+";\n" +
						"import org.springframework.stereotype.Service;\n" +
						"\n" +
						"import java.util.List;\n" +
						"\n" +
						"@Service\n" +
						"public interface "+table.getTableName()+"Service {\n" +
						"    \n" +
						"    List<"+table.getTableName()+"> findAll();\n" +
						"    \n" +
						"    void deleteAll();\n" +
						"}\n";

				String controller = "package "+path+".controller;\n" +
						"\n" +
						"import "+path+".entity."+table.getTableName()+";\n" +
						"import "+path+".service."+table.getTableName()+"Service;\n" +
						"import org.springframework.beans.factory.annotation.Autowired;\n" +
						"import org.springframework.http.ResponseEntity;\n" +
						"import org.springframework.stereotype.Controller;\n" +
						"import org.springframework.web.bind.annotation.DeleteMapping;\n" +
						"import org.springframework.web.bind.annotation.GetMapping;\n" +
						"\n" +
						"import java.util.List;\n" +
						"\n" +
						"@Controller\n" +
						"public class "+table.getTableName()+"Controller {\n" +
						"\n" +
						"    @Autowired\n" +
						"    private "+table.getTableName()+"Service "+table.getTableName()+"Service;\n" +
						"\n" +
						"    @GetMapping(\"/"+table.getTableName()+"s\")\n" +
						"    public ResponseEntity getAll"+table.getTableName()+"(){\n" +
						"        List<"+table.getTableName()+"> list"+table.getTableName()+" = "+table.getTableName()+"Service.findAll();\n" +
						"        return ResponseEntity.ok(list"+table.getTableName()+");\n" +
						"    }\n" +
						"\n" +
						"    @DeleteMapping(\"/"+table.getTableName()+"s\")\n" +
						"    public ResponseEntity<String> delete(){\n" +
						"        "+table.getTableName()+"Service.deleteAll();\n" +
						"        return ResponseEntity.ok(\"Delete all\");\n" +
						"    }\n" +
						"}\n";
				path = path.replace(".", "/");
				Path entityPath = Paths.get("src/main/java/"+path+"/entity/"+table.getTableName()+".java");
				Path repositoryPath = Paths.get("src/main/java/"+path+"/repository/"+table.getTableName()+"Repository.java");
				Path serviceImplPath = Paths.get("src/main/java/"+path+"/service/"+table.getTableName()+"ServiceImpl.java");
				Path servicePath = Paths.get("src/main/java/"+path+"/service/"+table.getTableName()+"Service.java");
				Path controllerPath = Paths.get("src/main/java/"+path+"/controller/"+table.getTableName()+"Controller.java");

				Files.write(entityPath, entity.getBytes(StandardCharsets.UTF_8));
				System.out.println("create" + "src/main/java/"+path+"/entity/"+table.getTableName()+".java");
			Files.write(repositoryPath, repository.getBytes(StandardCharsets.UTF_8));
			System.out.println("create" + "src/main/java/"+path+"/repository/"+table.getTableName()+"Repository.java");
			Files.write(serviceImplPath, serviceImpl.getBytes(StandardCharsets.UTF_8));
			System.out.println("create" + "src/main/java/"+path+"/service/"+table.getTableName()+"ServiceImpl.java");
			Files.write(servicePath, service.getBytes(StandardCharsets.UTF_8));
			System.out.println("create" + "src/main/java/"+path+"/service/"+table.getTableName()+"Service.java");
			Files.write(controllerPath, controller.getBytes(StandardCharsets.UTF_8));
			System.out.println("create" + "src/main/java/"+path+"/controller/"+table.getTableName()+"Controller.java");
			}


		}else {
			System.out.println("No generate API");
		}

	}

}

package com.example.Test;

import com.example.Test.entity.Column;
import com.example.Test.entity.TableInfo;
import com.example.Test.entity.TableRelationship;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

@SpringBootApplication
public class TestApplication implements ApplicationRunner  {

	@Value("${pathDir}")
	private String pathDir;

	@Value("${createAPI}")
	private Boolean createAPI;

	@Value("${createCSV}")
	private Boolean createCSV;

	@Value("${catalog}")
	private String catalog;

	@Value("${schemaPattern}")
	private String schemaPattern;

	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}

	@Override
	public void run( ApplicationArguments args ) throws Exception
	{
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
					tableListName.add(resultSet.getString("TABLE_NAME").substring(0,1).toUpperCase()
							+ resultSet.getString("TABLE_NAME").substring(1));
				}
			}

			// Gen file csv table relationship
			if(createCSV){
				createCSVFromDB(tableListName, databaseMetaData);
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
						try(ResultSet foreignKeys = databaseMetaData.getImportedKeys(catalog, schemaPattern, tableName)){
							while(foreignKeys.next()){

								String pkTableName = foreignKeys.getString("PKTABLE_NAME");
								String fkTableName = foreignKeys.getString("FKTABLE_NAME");
								String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
								String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
								if(columnName.equals(pkColumnName.toLowerCase())) {

									col.setPkTableName(pkTableName);
									col.setFkTableName(fkTableName);
									col.setPkColumnName(pkColumnName);
								}
								if(columnName.equals(fkColumnName.toLowerCase())){
									col.setFkColumnName(fkColumnName);
									col.setPkTableName(pkTableName);
									col.setFkTableName(fkTableName);
								}
							}
						}
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
		if(createAPI){
			// Read file CSV after update relationship
			Reader in = new FileReader("ER_Diagram.csv");
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
			List<TableRelationship> tableRelationships = new ArrayList<>();
			for (CSVRecord record : records) {
				TableRelationship tableRelationship = new TableRelationship();
				tableRelationship.setPkTableName(record.get(0));
				tableRelationship.setPkColumnName(record.get(1));
				tableRelationship.setFkTableName(record.get(2));
				tableRelationship.setFkColumnName(record.get(3));
				tableRelationship.setRelation(record.get(4));
				tableRelationships.add(tableRelationship);
			}
			// Xoa Header
			tableRelationships.remove(0);
			// Set relationship table
			for (TableInfo table: tableInfos) {
				for (Column col: table.getColumns()) {
					for (TableRelationship tableRelation: tableRelationships) {
						switch (tableRelation.getRelation()){
							case "1,1":
								if(tableRelation.getFkTableName().equals(table.getTableName().toLowerCase())
										&& tableRelation.getFkColumnName().equals(col.getName())){
									col.setRelation("OneToOne");
								}
								break;
							case "1,n" , "n,1":

								if(tableRelation.getPkTableName().equals(table.getTableName().toLowerCase())
									&& tableRelation.getPkColumnName().equals(col.getName())){
									col.setRelation("OneToMany");
								}
								if(tableRelation.getFkTableName().equals(table.getTableName().toLowerCase())
									&& tableRelation.getFkColumnName().equals(col.getName())){
									col.setRelation("ManyToOne");
								}
								break;
							case "n,n":
								System.out.println("chua xu ly");
								break;
							default:
								break;
						}
					}
				}
			}
		}
		// mvn spring-boot:run -Dspring-boot.run.arguments="--entity.name=Comment --create=true --path=com.example.Test"
		if(createAPI){
			// TODO Kiem tra kieu du lieu cua column - Datatype dang la kieu so: 1-int
			for (TableInfo table: tableInfos) {
				String column ="";
				for (Column col: table.getColumns() ) {
					if(col.getPrimaryKey() !=null ){

							column += "    @Id\n" +
									"    @GeneratedValue(strategy = GenerationType.AUTO)\n" +
									"    private long id; \n";

					}else {
							if(col.getRelation() != null){
								switch (col.getRelation()){
									case "OneToOne":
										column += "    @OneToOne()\n" +
												"    @JoinColumn(name = \""+col.getFkColumnName()+"\", referencedColumnName = \"id\")\n" +
												"    private "+col.getPkTableName().substring(0,1).toUpperCase()+
															col.getPkTableName().substring(1)+" "+col.getPkTableName().toLowerCase()+"; \n";
										break;
									case "OneToMany":
										column += "@OneToMany(mappedBy = \""+col.getPkTableName()+"\")\n" +
												"    private List<"+table.getTableName()+"> "+col.getFkTableName()+";";
										break;
									case "ManyToOne":
										column += "    @ManyToOne\n" +
												"    @JoinColumn(name=\""+col.getFkColumnName()+"\")\n" +
												"    private "+col.getPkTableName().substring(0,1).toUpperCase()+
																col.getPkTableName().substring(1)+" "+col.getPkTableName()+";";
										break;
									case "ManyToMany":
										System.out.println("chua dinh xu ly n-n");
										break;
									default:

								}
							}else {
								column += "    @Column(name = \""+col.getName()+"\", nullable = true)\n" +
										"    private long "+col.getName()+";\n";
							}
					}
				}

				String entity ="package "+pathDir+".entity;\n" +
						"\n" +
						"import lombok.AllArgsConstructor;\n" +
						"import lombok.Getter;\n" +
						"import lombok.NoArgsConstructor;\n" +
						"import lombok.Setter;\n" +
						"import java.util.List;\n"+
						"\n" +
						"import javax.persistence.*;\n" +
						"import javax.persistence.Column;\n" +
						"\n" +
						"@Entity\n" +
						"@Table(name = \""+table.getTableName()+"\")\n" +
						"@Setter\n" +
						"@Getter\n" +
						"@AllArgsConstructor\n" +
						"@NoArgsConstructor\n" +
						"public class "+table.getTableName()
						+" {\n" + column + "}\n";
				String repository = "package "+pathDir+".repository;\n" +
						"\n" +
						"import org.springframework.data.jpa.repository.JpaRepository;\n" +
						"import org.springframework.stereotype.Repository;\n" +
						"import "+pathDir+".entity."+table.getTableName()+";\n" +
						"\n" +
						"@Repository\n" +
						"public interface "+table.getTableName()+"Repository extends JpaRepository<"+table.getTableName()+", Long> {\n" +
						"}" ;

				String serviceImpl ="package "+pathDir+".service;\n" +
						"\n" +
						"import "+pathDir+".entity."+table.getTableName()+";\n" +
						"import "+pathDir+".repository."+table.getTableName()+"Repository;\n" +
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

				String service ="package "+pathDir+".service;\n" +
						"\n" +
						"import "+pathDir+".entity."+table.getTableName()+";\n" +
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

				String controller = "package "+pathDir+".controller;\n" +
						"\n" +
						"import "+pathDir+".entity."+table.getTableName()+";\n" +
						"import "+pathDir+".service."+table.getTableName()+"Service;\n" +
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
				pathDir = pathDir.replace(".", "/");
				Path entityPath = Paths.get("src/main/java/"+pathDir+"/entity/"+table.getTableName()+".java");
				Path repositoryPath = Paths.get("src/main/java/"+pathDir+"/repository/"+table.getTableName()+"Repository.java");
				Path serviceImplPath = Paths.get("src/main/java/"+pathDir+"/service/"+table.getTableName()+"ServiceImpl.java");
				Path servicePath = Paths.get("src/main/java/"+pathDir+"/service/"+table.getTableName()+"Service.java");
				Path controllerPath = Paths.get("src/main/java/"+pathDir+"/controller/"+table.getTableName()+"Controller.java");

				Files.write(entityPath, entity.getBytes(StandardCharsets.UTF_8));
//				System.out.println("create" + "src/main/java/"+pathDir+"/entity/"+table.getTableName()+".java");
			Files.write(repositoryPath, repository.getBytes(StandardCharsets.UTF_8));
//			System.out.println("create" + "src/main/java/"+pathDir+"/repository/"+table.getTableName()+"Repository.java");
			Files.write(serviceImplPath, serviceImpl.getBytes(StandardCharsets.UTF_8));
//			System.out.println("create" + "src/main/java/"+pathDir+"/service/"+table.getTableName()+"ServiceImpl.java");
			Files.write(servicePath, service.getBytes(StandardCharsets.UTF_8));
//			System.out.println("create" + "src/main/java/"+pathDir+"/service/"+table.getTableName()+"Service.java");
			Files.write(controllerPath, controller.getBytes(StandardCharsets.UTF_8));
//			System.out.println("create" + "src/main/java/"+pathDir+"/controller/"+table.getTableName()+"Controller.java");
				pathDir = pathDir.replace("/", ".");
			}


		}else {
			System.out.println("Skip generate API....");
		}

	}

	public void createCSVFromDB(List<String> tableListName, DatabaseMetaData databaseMetaData) throws IOException, SQLException {
		String[] HEADERS = { "PK table", "PK Column", "FK table", "FK column", "Relation(PK,FK)"};
		FileWriter out = new FileWriter("ER_Diagram.csv");
		List<TableRelationship> tableRelationships = new ArrayList<>();
		for (String tableName: tableListName) {
			try(ResultSet foreignKeys = databaseMetaData.getImportedKeys(catalog, schemaPattern, tableName)){
				while(foreignKeys.next()){
					TableRelationship tableRelationship = new TableRelationship();
					String pkTableName = foreignKeys.getString("PKTABLE_NAME");
					tableRelationship.setPkTableName(pkTableName);
					String fkTableName = foreignKeys.getString("FKTABLE_NAME");
					tableRelationship.setFkTableName(fkTableName);
					String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
					tableRelationship.setPkColumnName(pkColumnName);
					String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
					tableRelationship.setFkColumnName(fkColumnName);
					tableRelationships.add(tableRelationship);
				}
			}
		}
		try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
				.withHeader(HEADERS))) {
			for (TableRelationship tableRelationship: tableRelationships) {
				printer.printRecord(tableRelationship.getPkTableName(), tableRelationship.getPkColumnName(),
						tableRelationship.getFkTableName(), tableRelationship.getFkColumnName());
			}
		}
	}

}

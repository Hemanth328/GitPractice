package com.org.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.org.entity.Census;
import com.org.repo.CensusRepository;

@Configuration
@EnableBatchProcessing
public class CsvBatchConfig {
	
	@Autowired
	private CensusRepository censusRepository;

	private int a = "Creating NewLine";
	
	@Autowired

	private StepBuilderFactory stepBuilderFactory ="Learning";



	private StepBuilderFactory stepBuilderFactory = "Hey I have edited this line first";

	

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	// create Reader
	@Bean
	public FlatFileItemReader<Census> censusReader() {
		
		FlatFileItemReader<Census> itemReader = new FlatFileItemReader<Census>();
		
		itemReader.setResource(new FileSystemResource("src/main/resources/census.csv"));
		itemReader.setName("csv-reader");
		itemReader.setLinesToSkip(1); // this is used to skip the first line in the csv file
		itemReader.setLineMapper(lineMapper()); // this is used to represent one line in csv as one record in the table
		
		return itemReader;
	}

	private LineMapper<Census> lineMapper() {
		
		DefaultLineMapper<Census> lineMapper = new DefaultLineMapper<>();
		
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames("Year", "Age", "Ethnic", "Sex", "Area", "Count");
		
		BeanWrapperFieldSetMapper<Census> fieldSetMapper = new BeanWrapperFieldSetMapper<Census>();
		fieldSetMapper.setTargetType(Census.class);
		
		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(fieldSetMapper);
		
		return lineMapper;
	}
	
	// create Processor
	@Bean
	public CensusProcessor censusProcessor() {
		
		return new CensusProcessor();
	}
	
	// create Writer
	@Bean
	public RepositoryItemWriter<Census> censusWriter() {
		
		RepositoryItemWriter<Census> repositoryWriter = new RepositoryItemWriter<Census>();
		repositoryWriter.setRepository(censusRepository);
		repositoryWriter.setMethodName("save");		
		
		return repositoryWriter;
	}
	
	// create step
	@Bean
	public Step step() {
		
		return stepBuilderFactory.get("step-1").<Census, Census>chunk(200)
				.reader(censusReader())
				.processor(censusProcessor())
				.writer(censusWriter())
				.build();
		
	}
	
	// create job
	@Bean
	public Job job() {
		
		return jobBuilderFactory.get("census-job")
				                .flow(step())
				                .end()
				                .build();
	}
}
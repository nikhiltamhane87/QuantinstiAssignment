package com.quanInsti.tests;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;

import com.quantInsti.base.TestBase;


public class CoursePage extends TestBase  {
	
	//Testcase for getting the respective course details
	@Test(priority=3)
	public void getCourseDetails() {
		
				
		WebElement courseName = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div/main/div/div[1]/div[2]/div/div[1]/h1"));
		System.out.println("The Name of the Course is : " +courseName.getText());
		
		WebElement coursePrice = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div/main/div/div[1]/div[2]/div/div[2]/div[1]/div[3]/div[2]/span[2]/span"));
		System.out.println("The Price of  is of the course is : " +coursePrice.getText());
		
			
	}
	
	//Testcase for enrolling to a course 
	@Test(priority=4)
	public void clickOnEnrollNow() throws InterruptedException {
		
		driver.findElement(By.xpath("//*[@id=\"courseDetailMenu\"]/div/ul/li[8]/span/button/span/span")).click();
		Thread.sleep(5000);
	}
	
	
	

}

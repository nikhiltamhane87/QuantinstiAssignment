package com.quanInsti.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;

import com.quantInsti.base.TestBase;

public class LoginPage extends TestBase  {
	
	//Testcase for checking login functionality
	@Test(priority=1)
	public void loginAs() {
		
				
		driver.findElement(By.xpath("//*[@id=\"right-navigation\"]/ul/div[1]/li[2]/button")).click();
		driver.findElement(By.xpath("//input[@name='email']")).sendKeys("nikhil.tamhane87@gmail.com");
		driver.findElement(By.xpath("//input[@name='password']")).sendKeys("nikhil_10");
		driver.findElement(By.xpath("//button[@type='submit']")).click();
	}
	
	//Testcases for selecting "Sentiment Analysis in Trading" menu options
	@Test(priority=2)
	public void selectOptionFromMenu() throws InterruptedException {
		
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[2]/div[2]/div[1]/div/div/div[2]/div[1]/button"))).click();
			
		action.moveToElement(driver.findElement(By.xpath("//*[@id=\"right-navigation\"]/ul/div[1]/li[4]/span/a"))).perform();	
		driver.findElement(By.xpath("//*[@id=\"right-navigation\"]/ul/div[1]/li[4]/div/div[2]/ul/li[7]/a/span")).click();
		
		Thread.sleep(5000);
		
	}

	

}

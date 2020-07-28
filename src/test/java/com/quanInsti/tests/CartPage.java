package com.quanInsti.tests;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;

import com.quantInsti.base.TestBase;

public class CartPage extends TestBase {
	
	//Testcase for counting the courses on the cartpage
	@Test(priority=5)
	public void getCourseNamesAndCount() throws InterruptedException {
		
		
		       	        
			WebElement courseCart = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div/main/div/div/div/div[1]/div/div[2]"));
			
			List<WebElement> courses = courseCart.findElements(By.xpath("//div[@class='cart-item']"));
			int courseCount = courses.size();
			
			System.out.println("The Total Number of courses added in the cart are :" +courseCount);
			
			for(int i=1; i<=courseCount; i++) {
				
				
				String courseName = courseCart.findElement(By.xpath("//div[@class='cart-item']["+i+"]/div[1]/div/div[1]/span")).getText();
				//String courseName = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div/main/div/div/div/div[1]/div/div[2]/div["+i+"]/div[1]/div/div[1]/span")).getText();
				System.out.println("The Name of Course Number " +i+ " is : " +courseName);
				
			}
			
			String cartCount = driver.findElement(By.xpath("//*[@id=\"right-navigation\"]/ul/div[1]/li[3]/a/span")).getText();

			int totalCartCount = Integer.parseInt(cartCount);
			System.out.println("The Cart Count is : " +totalCartCount);
			
			if(courseCount==totalCartCount)
				System.out.println("The Total Number of course items displayed on cart page matches with the cart count");
			
			else
				System.out.println("There is mismatch between Total Number of course items displayed on cart page matches and the cart count");
			
			
			
	}
	
	//Testcases for fecting the different prices
	@Test(priority=6)
	public void getPriceDetails() {
		
		String baseAmout = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div/main/div/div/div/div[2]/div[1]/div[2]/div[2]")).getText();
		System.out.println("The Base Amount is : " +baseAmout);
		
		String amountPayable = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div/main/div/div/div/div[2]/div[1]/div[6]/div[2]/h5/span")).getText();
		System.out.println("The Amount Payable is :" +amountPayable);
		
	}
	
	//Testcase for viewing a particular course
	@Test(priority=7)
	public void viewCourseDetails() throws InterruptedException {
		
		
		driver.findElement(By.xpath("//a[@href='/course/trading-twitter-sentiment-analysis']")).click();
		Thread.sleep(5000);
		
		Set<String> windowHandles = driver.getWindowHandles();
		Iterator<String> iterate = windowHandles.iterator();

		String parentwindow = iterate.next(); 
		
		String childwindow = iterate.next();
		
		driver.switchTo().window(childwindow);
		driver.close();
		
		driver.switchTo().window(parentwindow);

		
 
	}
	
	//Testcase to remove an course from cartpage
	@Test(priority=8)
	public void removeCourse() throws InterruptedException {
		
		
			
		driver.findElement(By.xpath("/html/body/div[2]/div[2]/div/main/div/div/div/div[1]/div/div[2]/div[2]/div[1]/div/div[2]/div/a[2]")).click();
				
		
	}
	
	//Testcase to apply a coupon
	@Test(priority=9)
	public void applyCoupon() {
		
		driver.findElement(By.xpath("/html/body/div[2]/div[2]/div/main/div/div/div/div[2]/div[1]/div[3]/button/span/span")).click();
		
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"coupon-modal\"]/div[2]/div/div/form/div[2]/input"))).sendKeys("ABC");
		driver.findElement(By.xpath("//*[@id=\"coupon-modal\"]/div[2]/div/div/form/div[3]/button")).click();
		
		driver.findElement(By.xpath("//*[@id=\"coupon-modal\"]/div[2]/div/div/button/span")).click();
		
	}
	
	
	//Testcase to logout
	@Test(priority=10)
	public void signOut() throws InterruptedException {
		
		driver.findElement(By.xpath("//*[@id=\"right-navigation\"]/ul/div[1]/li[4]/div[1]")).click();
		
		driver.findElement(By.xpath("//*[@id=\"right-navigation\"]/ul/div[1]/li[4]/div[2]/ul/li[5]/a")).click();
		
		Thread.sleep(5000);
		
	}
			
		
		

}

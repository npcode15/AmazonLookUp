package bookslookup;

import java.io.*;
import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.net.URLConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

class BooksLookUp
{
	public static void main(String[] args) throws IOException
	{
		try 
		{
			List<String> isbns = readIsbn();
			sequentialRun(isbns);
			concurrentRun(isbns);
		} 
		catch (IOException e) 
		{
			e.getMessage();
		}
		catch (Exception e) 
		{
			e.getMessage();
		}
	}

	public static void concurrentRun(List<String> isbnList) throws Exception
	{
		List<Book> listOfBooks = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(15);

		long startTime = System.nanoTime();
		for(String isbn: isbnList)
		{
			Runnable thread = new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						listOfBooks.add(findDetailsForABook(isbn));
					}
					catch(Exception e)
					{
						try 
						{
							throw new Exception();
						} 
						catch (Exception e1) 
						{
							e1.printStackTrace();
						}	
					}
				}
			};
			executor.submit(thread);
		}

		Runnable secThread = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					executor.awaitTermination(5, TimeUnit.SECONDS);
					long endTime = System.nanoTime();
					List<Book> sortedBookList = sortBookList(listOfBooks);
					displayBookDetails(sortedBookList);
					System.out.println("Concurrent Execution Time: " + (endTime - startTime)/1000000000 + " Seconds\n\n");
				}
				catch (InterruptedException e)
				{
					try 
					{
						throw new InterruptedException();
					}
					catch (Exception e1) 
					{
					}
				}
			}
		};
		executor.submit(secThread);
	}

	public static void sequentialRun(List<String> isbnList) throws Exception
	{
		try 
		{
			long startTime = System.nanoTime();
			List<Book> listOfBooks = findDetailsForAllBooks(isbnList);
			long endTime = System.nanoTime();
			List<Book> sortedBookList = sortBookList(listOfBooks);
			displayBookDetails(sortedBookList);
			System.out.println("Sequential Execution Time: " + (endTime - startTime)/1000000000 + " Seconds\n\n");
		} 
		catch (Exception e) 
		{
			throw new Exception();
		}
	}

	public static List<String> readIsbn() throws IOException
	{    
		return Files.lines(Paths.get(System.getProperty("user.dir") + "/bookslookup/ISBNPool.txt"))
				    .collect(toList());
	}

	public static List<Book> findDetailsForAllBooks(List<String> isbnList)
	{
		return isbnList.stream()
				       .map(BooksLookUp::findDetailsForABook)
				       .collect(toList());
	}

	public static Book findDetailsForABook(String isbn)
	{
		try 
		{
		BufferedReader inputStream;

		URL baseUrl;
		
		baseUrl = new URL("https://www.amazon.com/exec/obidos/ASIN/");
		
		URL currentUrl = new URL(baseUrl, isbn);

		URLConnection connection = (currentUrl).openConnection();
		connection.setRequestProperty("Content-Type", "text/html");
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36");

		inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		String title = "";
		String rank = "";
		String currentLine = "";
		while((currentLine = inputStream.readLine()) != null)
		{	
			if(currentLine.contains("<meta name=\"title\" content="))
				title = extractTitle(currentLine);

			if(currentLine.contains("in Books"))
				rank = extractRank(currentLine);
		}

		if(rank == "")
			rank = "0";

		return new Book(title, isbn, rank);
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return null;
	}

	public static String extractTitle(String currentLine)
	{
		String title = "";

		String [] titleLine = currentLine.split("content");
		String [] newTitleLine = titleLine[1].substring(2).split(":");			
		title = newTitleLine[0];

		return title;
	}

	public static String extractRank(String currentLine)
	{
		String rank = "";

		String[] rankLine = currentLine.split(" ");
		rank = rankLine[0].substring(1);

		return rank;
	}

	public static List<Book> sortBookList(List<Book> bookList)
	{                     
		return bookList.stream()
				       .sorted(comparing(Book::getRank))
				       .collect(toList());                                                                                 
	}

	private static void displayBookDetails(List<Book> bookList) 
	{
		for(Book book : bookList)
			System.out.println(book);
	}
}
package bookslookup;

class Book
{
  private String title;
  private String isbn;
  private String rank; //Venkat: private int rank

  public Book(String title, String isbn, String rank) //Venkat: int rank
  {
    this.title = title;
    this.isbn = isbn;
    this.rank = rank;
  }

  public String getTitle() 
  {
    return title;
  }

  public String getIsbn() 
  {
    return isbn;
  }

  public Integer getRank() 
  {
    return Integer.parseInt(rank.replace(",",""));
  }

  public String toString()
  {
    return String.format("%-100s %-14s %-14s", title, isbn, rank);
  }
}
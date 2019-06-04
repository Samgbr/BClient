package com.mycompany.binferprojectclient;

import com.mycompany.binferprojectclient.model.Book;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

public class FXMLController implements Initializable {

    @FXML
    private TextField input;
    @FXML
    private ListView<String> list;
    @FXML
    private Button save_btn;
    @FXML
    private Button delete_btn;
    @FXML
    private TableView<Book> tableview1;
    @FXML
    private TableColumn<Book, Integer> id;
    @FXML
    private TableColumn<Book, String> bookTitle;
    @FXML
    private TableColumn<Book, CheckBox> selectCol;
    
    private final Book book = new Book();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) { 
        List result = restHttp();
        FilteredList<String> filteredList = displayListViewItems(result);
        searchTextFieldListener(filteredList);
        //TableView
        selectCol.setCellValueFactory(new PropertyValueFactory<>("select"));
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        bookTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        tableview1.setItems(getBookList());
        saveButtonListener();
    }
    /**
     * This method handles the delete button
     * @param event 
     */
    @FXML
    private void deleteSelectedRows(ActionEvent event) {
        //System.out.println("Reached here...");
        Boolean b = false;
        for(int i=0;i<tableview1.getItems().size();i++) {
            Book bk = new Book();
            bk = tableview1.getItems().get(i);
            if(bk.getSelect().isSelected()) {
                deleteRest(bk.getId());
                b=true;
            }
        }
        if(b) {
            Alert deleteAlert = new Alert(AlertType.CONFIRMATION);
            deleteAlert.setHeaderText("Confirmation");
            deleteAlert.setContentText("Book deleted.");
            deleteAlert.showAndWait();   
        } else {
            Alert errorAlert = new Alert(AlertType.ERROR);
            errorAlert.setHeaderText("Delete Error");
            errorAlert.setContentText("Please select a book from tableview to delete");
            errorAlert.showAndWait();
        }
        tableview1.setItems(getBookList());
    }
    
    /**
     * This method reads the NY API Key from text file
     * @return 
     */
    private String readKey() {
        StringBuilder sb = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new FileReader("C:/Users/samzi/Documents/NetBeansProjects/key.txt"))) {
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            //System.out.println(sb.toString());
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }
    
    /**
     * This method deletes book by id
     * @param id - book id
     */
    private void deleteRest(int id) {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        //System.out.println(id);
        HttpDelete deleteRequest = new HttpDelete("http://localhost:8080/deleteBook/"+id);
        //System.out.println("http://localhost:8080/deleteBook/"+id);
        try {
            HttpResponse response = client.execute(deleteRequest);
            System.out.println(response.toString());
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This method handles the save button
     */
    private void saveButtonListener() {
        save_btn.setOnAction((event) -> {
            //check if the book is saved or not using
            if(book.getBookTitle()!=null && checkRestGet()) {
                restPost();
                System.out.println("Book saved");
                Alert saveAlert = new Alert(AlertType.CONFIRMATION);
                saveAlert.setHeaderText("Confirmation");
                saveAlert.setContentText("Book saved.");
                saveAlert.showAndWait();
            } else {
                Alert errorAlert = new Alert(AlertType.ERROR);
                errorAlert.setHeaderText("Save Error");
                errorAlert.setContentText("Please select a book title to save or book already saved");
                errorAlert.showAndWait();
            }
           tableview1.setItems(getBookList()); 
        });
    }
    
    /**
     * This method will look for book titles in a list view
     * @param filteredList - list of book titles
     */
    private void searchTextFieldListener(FilteredList<String> filteredList) {
        input.textProperty().addListener(obs->{
            String filter = input.getText();
            if(filter == null || filter.length() == 0) {
                filteredList.setPredicate(s -> true);
            }
            else {
                filteredList.setPredicate(s -> s.toLowerCase().contains(filter));
            }
        });
    }
    
    /**
     * This method will display list view items from the NY API
     * @param result - List of book titles from public REST API
     * @return - filtered list
     */
    private FilteredList<String> displayListViewItems(List result) {
        ObservableList<String> observableList = FXCollections.observableArrayList(result);
        FilteredList<String> filteredList = new FilteredList<>(observableList);
        list.setCellFactory(CheckBoxListCell.forListView(new Callback<String, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(String item) {
                BooleanProperty observable = new SimpleBooleanProperty();
                observable.addListener((obs, wasSelected, isNowSelected)->
                {
                    System.out.println("Check box for "+item+" changed from "+wasSelected+" to "+isNowSelected);
                    book.setBookTitle(item);
                });
                return observable ;
            }
        }));
        list.setItems(filteredList);
        return filteredList;
    }
    
    /**
     * This method checks if the book to be saved exists in the h2 database
     * @return -true/false
     */
    private Boolean checkRestGet() {
        String result = retrieveData();
        JSONArray books = new JSONArray(result);
                for(int i=0;i<books.length();i++) {
                    JSONObject o = books.getJSONObject(i);
                    if(o.getString("bookTitle").equalsIgnoreCase(book.getBookTitle())) {
                        return false; 
                    }
                } 
     return true;   
    }
    
    /**
     * This method lists books saved from the h2 database
     * @return - filtered list
     */
    private ObservableList<Book> getBookList() {
        //ArrayList<String> booksList = new ArrayList<>();
        //ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<Book> bookList = new ArrayList<>();
        String result = retrieveData();
        JSONArray books = new JSONArray(result);
                for(int i=0;i<books.length();i++) {
                    JSONObject o = books.getJSONObject(i);
                    String bkTitle = o.getString("bookTitle");
                    Integer bid = o.getInt("id");
                    bookList.add(new Book(bid,bkTitle));
                    //booksList.add(bkTitle);
                    //ids.add(bid);
                }
        ObservableList<Book> observableList = FXCollections.observableArrayList(bookList);
        //FilteredList<String> filteredList = new FilteredList<>(observableList);
        return observableList;
    }
    
    /**
     * This method get all books in the h2 database
     * @return 
     */
    private String retrieveData() {
        String result="";
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet("http://localhost:8080/getAllBooks");
        getRequest.addHeader("accept","application/json");
        try {
            HttpResponse response = client.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
			   + response.getStatusLine().getStatusCode());
		}
            //Test code to print the response 
            BufferedReader br = new BufferedReader(
                         new InputStreamReader((response.getEntity().getContent())));
		String output;
		System.out.println("Getting all books from the Server ....");
		while ((output = br.readLine()) != null) {
                    System.out.println(output);
                    result=output;
		}
                
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    /**
     * This method save book instance on server h2 database
     */
    private void restPost() {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpPost postRequest = new HttpPost("http://localhost:8080/saveBook");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("bookTitle",book.getBookTitle());
        try {
            StringEntity se = new StringEntity(jsonObject.toString());
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
            postRequest.setEntity(se);
            HttpResponse response = client.execute(postRequest);
            System.out.println(response.toString());
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This method will request HTTP from the NY Best seller books API to get top 15 ranked book titles
     * @return - list of book titles
     */
    private ArrayList<String> restHttp() {
        ArrayList<String> choices = new ArrayList<>();
        String API_Key = readKey().trim();
        String appURI = "https://api.nytimes.com/svc/books/v3/lists/current/hardcover-fiction.json?api-key="+API_Key;
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(appURI);
        getRequest.addHeader("accept","application/json");
        try {
            HttpResponse response = client.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
			   + response.getStatusLine().getStatusCode());
		}
            
            //Test code to print the response 
            BufferedReader br = new BufferedReader(
                         new InputStreamReader((response.getEntity().getContent())));
                String result="";
		String output;
		System.out.println("Output from Server .... \n");
		while ((output = br.readLine()) != null) {
                    System.out.println(output);
                    result=output;
		}
            System.out.println(result);
            JSONObject obj = new JSONObject(result);
            JSONObject bo = obj.getJSONObject("results");
            JSONArray books = bo.getJSONArray("books");
            for(int i=0;i<books.length();i++) {
                JSONObject o = books.getJSONObject(i);
                String bkTitle = o.getString("title");
                choices.add(bkTitle);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Testing book titles on console
        for(int j=0;j<choices.size();j++) {
            System.out.println(choices.get(j));
        }
        return choices;
    }
}
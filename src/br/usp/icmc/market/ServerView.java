package br.usp.icmc.market;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ServerView extends Scene
{
	private Controller controller;
	private CSVManager manager;

	private TableView<Product> products;
	private TableView<User> users;

	private TextField userSearch;
	private TextField productSearch;

	private Alert error = new Alert(Alert.AlertType.ERROR);

	public ServerView(Pane pane)
	{
		super(pane);
		controller = Controller.getInstance();
		manager = new CSVManager();

		products = new TableView<>();

		/* MENU BLOCK */
		MenuBar menuBar = new MenuBar();
		Menu menuOption = new Menu("Options");


		MenuItem menuSupplies = new MenuItem("Supplies");

		menuOption.getItems().addAll(menuSupplies);
		menuBar.getMenus().addAll(menuOption);

		/* DATE PICKER BLOCK */
		Alert datePickerModal = new Alert(Alert.AlertType.CONFIRMATION);
		DatePicker datePicker = new DatePicker();
		datePickerModal.setTitle("Choose a Date");
		datePickerModal.setHeaderText("Choose a date!");
		datePickerModal.getDialogPane().setContent(datePicker);

		/* TABS BLOCK */
		userSearch = new TextField();
		userSearch.setPromptText("Search user by login or name");
		Button addUser = new Button("Add User");
		Button removeUser = new Button("Remove User");
		HBox hBoxUsersTab = new HBox(addUser, removeUser);
		hBoxUsersTab.setAlignment(Pos.CENTER);
		VBox vBoxUsersTab = new VBox(userSearch, users, hBoxUsersTab);
		Tab usersTab = new Tab("Users", vBoxUsersTab);
		usersTab.setClosable(false);

		productSearch = new TextField();
		productSearch.setPromptText("Search product by id or title");
		Button addBook = new Button("Add Book");
		Button removeBook = new Button("Remove Book");
		Button lendBook = new Button("Lend Book");
		HBox hBoxBooksTab = new HBox(addBook, removeBook, lendBook);
		hBoxBooksTab.setAlignment(Pos.CENTER);
		VBox vBoxBooksTab = new VBox(productSearch, products, hBoxBooksTab);
		Tab booksTab = new Tab("Books", vBoxBooksTab);
		booksTab.setClosable(false);

		TabPane tabPane = new TabPane(usersTab, booksTab);
		VBox verticalPane = new VBox(menuBar, tabPane);
		verticalPane.setAlignment(Pos.TOP_CENTER);

		verticalPane.setPrefWidth(800);
		verticalPane.setPrefHeight(600);
		pane.getChildren().add(verticalPane);
		/* END TABS BLOCK */


		/* SETUP FUNCTIONS*/
		addColumns();
		fillTable();
		setAddUserButton(addUser);
		setRemoveUserButton(removeUser);
		setAddBookButton(addBook);
		setRemoveBookButton(removeBook);
		setLendButton(lendBook);
		setReturnButton(returnBook);
		/* END SETUP FUNCTIONS*/

		/* SHOWS DATE PICKER ON START */
		ButtonType returnValue = datePickerModal.showAndWait().get();
		if (returnValue == null || returnValue.equals(ButtonType.CANCEL))
			System.exit(0);
		try
		{
			LocalDate date = datePicker.getValue();
			controller.setDate(date);
			currentDate.setText("Current date: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void addColumns()
	{
		addUserColumns();
		addBookColumns();
	}

	/*
		TABLE COLUMNS BLOCK
		Add columns to user, book and loan tables
		Configure columns
	 */
	private void addUserColumns()
	{
		TableColumn<User, String> type = new TableColumn<>("Type");
		type.setCellValueFactory(new PropertyValueFactory<>("type"));
		TableColumn<User, String> userId = new TableColumn<>("Login");
		userId.setCellValueFactory(new PropertyValueFactory<>("login"));
		TableColumn<User, String> name = new TableColumn<>("Name");
		name.setCellValueFactory(new PropertyValueFactory<>("name"));
		TableColumn<User, String> cpf = new TableColumn<>("CPF");
		cpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));
		TableColumn<User, String> address = new TableColumn<>("Address");
		address.setCellValueFactory(new PropertyValueFactory<>("address"));
		TableColumn<User, String> contact = new TableColumn<>("Contact");
		contact.setCellValueFactory(new PropertyValueFactory<>("contact"));
		TableColumn<User, String> email = new TableColumn<>("E-mail");
		email.setCellValueFactory(new PropertyValueFactory<>("email"));
		TableColumn<User, String> banned = new TableColumn<>("Banned Until");
		banned.setCellValueFactory(new PropertyValueFactory<>("banDate"));

		users.getColumns().addAll(type, userId, name, cpf, address, contact, email, banned);
		users.selectionModelProperty().get().setSelectionMode(SelectionMode.SINGLE);
	}

	private void addProductsColumns()
	{
		TableColumn<Product, String> type = new TableColumn<>("Type");
		type.setCellValueFactory(new PropertyValueFactory<>("type"));
		TableColumn<Product, Integer> bookId = new TableColumn<>("ID");
		bookId.setCellValueFactory(new PropertyValueFactory<>("id"));
		TableColumn<Product, String> title = new TableColumn<>("Title");
		title.setCellValueFactory(new PropertyValueFactory<>("title"));
		TableColumn<Product, String> author = new TableColumn<>("Author");
		author.setCellValueFactory(new PropertyValueFactory<>("author"));
		TableColumn<Product, String> publisher = new TableColumn<>("Publisher");
		publisher.setCellValueFactory(new PropertyValueFactory<>("publisher"));
		TableColumn<Product, Integer> year = new TableColumn<>("Year");
		year.setCellValueFactory(new PropertyValueFactory<>("year"));
		TableColumn<Product, Integer> pages = new TableColumn<>("# of Pages");
		pages.setCellValueFactory(new PropertyValueFactory<>("pages"));
		TableColumn<Product, String> availability = new TableColumn<>("Available?");
		availability.setCellValueFactory(new PropertyValueFactory<>("isAvailable"));

		books.getColumns().addAll(type, bookId, title, author, publisher, year, pages, availability);
		books.selectionModelProperty().get().setSelectionMode(SelectionMode.SINGLE);
	}

	/*
		Fill tables with controller's data
		Create SortedList and binds a comparator
	 */
	private void fillTable()
	{
		ObservableList<User> obsUser = controller.getUsers();
		ObservableList<Product> obsProduct = controller.getProductss();

		FilteredList<User> filteredUser = new FilteredList<>(obsUser, u -> true);
		FilteredList<Product> filteredBook = new FilteredList<>(obsProduct, b -> true);

		SortedList<User> sortedUser = new SortedList<>(filteredUser);
		SortedList<Product> sortedBook = new SortedList<>(filteredBook);

		sortedBook.comparatorProperty().bind(products.comparatorProperty());
		sortedUser.comparatorProperty().bind(users.comparatorProperty());

		userSearch.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredUser.setPredicate(user -> {
				if (newValue == null || newValue.isEmpty())
					return true;

				String userFilter = newValue.toLowerCase();

				if (user.getLogin().toLowerCase().contains(userFilter))
					return true;
				else if (user.getName().toLowerCase().contains(userFilter))
					return true;
				return false;
			});
		});

		productSearch.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredBook.setPredicate(product -> {
				if (newValue == null || newValue.isEmpty())
					return true;

				String bookFilter = newValue.toLowerCase();

				if (book.getTitle().toLowerCase().contains(bookFilter))
					return true;
				else if (Integer.toString(book.getId()).contains(bookFilter))
					return true;
				return false;
			});
		});


		users.setItems(sortedUser);
		products.setItems(sortedBook);
	}
}
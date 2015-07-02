# Online Market Simulator
This is an online market simulator. We can simulate the server that manages the operations, or a client that can buy products.

You start the server simulator by opening the Server.jar file, and the client simulator by opening the Client.jar file.
When you start the client simulator, you need to specify the Server's IP address. If the connection is successful, a login window will appear, where you can input your username and password or create a new user in case you don't have one.

On the server simulator, you can:
- View what users are registered on the system, as well as the products and their respective quantities
- Register new products, by switching to the products tab and clicking the "Add Product" button
- Update a product information (quantity and price), by selecting a product and clicking the "Update Product" button
- Restock your products, by clicking the "Ask provider" button, selecting a provider and placing an order
- Import and export the current users and products information (to a CSV file), by selecting the appropriate option on the "File" menu bar

On the client simulator, you can:
- Create a new user, by selecting the "New Button" user on the login window and filling the information
- View all the products registered on the server
- Select multiple products (with CTRL and SHIFT) and place an order by clicking the "Buy Selected Items" and adjusting the quantity of each one

Please note that, if the client wants to buy an unavailable product (a product with its "Quantity" property value of 0), the system will send a request to the server. Then, when the server restocks (via the "Ask Provider" button), the system will send an email to that client, informing that the items were back in stock, and ready to be bought.

Made with Java 1.8.0_40, using IntelliJ IDEA 14.1

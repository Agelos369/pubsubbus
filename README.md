# PubSub system for bus locations
## An Android app for receiving bus positions in real time.
The app is based on a multi threaded publish subscribe distributed arcitecture (backend). The user (Subscriber) can subscribe to any bus line
he wants in order to receive the data (location, bus stop etc) when these are available.
The Publishers get the data from the bus sensors when these are available (read them from a file) and sends them to the Brokers.
The Brokers sends the data to the corresponding subscribers. The connections between Subscribers and Brokers and Publishers and Brokers
are established using network sockets.
Each Publisher handle a specific range of busses and each broker is connected with specific Publishers. In general, when a new 
connection is established, a new thread is created to handle the connection. This way we can have multiple Publisher, Brokers and Subscribers.

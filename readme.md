A version of BitTorrent
I will be developing a distributed leaderless peer-to-peer file-sharing system for this project.
The idea for this project is derived from BitTorrent, which is available for concurrently sharing files among peers. 

Components:
Tracker node:  There will be one Tracker node. It will have the information of all the available peers and the file stored at them, along with other file-related data.
Peers: There will be multiple Peers in this system. Each peer node will be involved in downloading and uploading the file concurrently.

Startup:
The tracker node will start a server socket on the given Ip and Port. and will begin listening for the incoming request from the peers in the system. 
Peers node will start server sockets on its given Ip and Port and connect with the Tracker node to do the initial setup.

How downloading of data will happen?
The Peer who wants to download the file will go to the tracker node to get that file's related information.
That Peer will connect to all the available peers in the swarm whose information is provided by the tracker node.
For each file to be downloaded, the node will keep track of all the packets that are not downloaded yet. This list will be updated when any new packet is downloaded.
Threads will be downloading some random packets of this file from connections to the Peer in the swarm concurrently. 
If the packet downloaded is not already available. Then, it will be appended to the temporary file, and that packet number will be removed from the list. If it is already available, then it will be ignored. 
After downloading each packet, the node will send an update message to the tracker node. The tracker node will update the files and packets information and will send the updated information of that file to this node.
When this list of packets to be downloaded is empty, it means that the download of the entire file is successful. Now the connection which was used to pull data will be closed. And the rearrangement of the packets from the temporary file to the final file will happen.
Once all the packets are arranged, the status of this file will be set as downloaded.

How will uploading happen?
When the download status of the file is in downloading state, then the requested packet data will be extracted from the temporary file. If the packet is not available, it will send a response saying that packet is not yet available. 
If a file status is in the download state, the requested packet data will be extracted from the final file.

Milestone:
Setup Tracker Server
Setup Peer Server
Initial setup of the Peer to the Tracker node.
Create swarms for the files to be downloaded and uploaded.
Connecting to peers
Upload functionality
Download functionality
Final file reassembling from the temporary file after download.

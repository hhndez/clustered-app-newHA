When I tested it I used this configuration:

En endpoints->Durable queues
 
Input Queue: app1_input
Topic Subscription: order/*
 
State Queue1: app1_state1
Topic Subscription trade/app1/inst2/*
 
State Queue2: app1_state2
Topic Subscription trade/app1/inst1/*
 
 
Command for app1-inst1:
bin/run-app.sh 10.100.236.146 app1 1 poc_vpn user1 password app1_input a1_state1 trade/app1/inst1/new
 
Command for app1-inst2:
bin/run-app.sh 10.100.236.146 app1 2 poc_vpn user2 password app1_input a1_state2 trade/app1/inst2/new
 
Command for mock Order Gateway:
bin/run-order-gw.sh 10.100.236.146 poc_vpn pub pub order/new 1

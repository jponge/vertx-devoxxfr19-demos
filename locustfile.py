from locust import *
from random import *
import json

# --------------------------------------------------------------------------- #
# Ingestion to MongoDB
# --------------------------------------------------------------------------- #

# ingestion_client_id = 0
# def next_ingestion_client_id():
#   global ingestion_client_id
#   ingestion_client_id += 1
#   return ingestion_client_id

# class IngestionClient(TaskSet):

#     def on_start(self):
#       self.tyreId = str(next_ingestion_client_id())
#       self.pressure = 12.0 + randint(0, 5)

#     @task(4)
#     def send_pressure_update(self):
#         delta = random()
#         if (randint(1, 2) % 2 is 0):
#           self.pressure += delta
#         else:
#           self.pressure -= delta
#         data = json.dumps({"tyreId": self.tyreId, "pressure": self.pressure})
#         self.client.post("http://localhost:3000/ingest", data=data)

#     @task(1)
#     def get_last_five(self):
#         self.client.get("http://localhost:3000/last/5/" + self.tyreId)

# class IngestionUser(HttpLocust):
#     task_set = IngestionClient
#     host = "localhost"
#     min_wait = 5000
#     max_wait = 10000

# --------------------------------------------------------------------------- #
# Edge service
# --------------------------------------------------------------------------- #

# edge_client_id = 0
# def next_edge_client_id():
#   global edge_client_id
#   edge_client_id += 1
#   return edge_client_id

# class EdgeClient(TaskSet):

#     def on_start(self):
#       self.tyreId = str(next_edge_client_id())

#     @task
#     def ask_for_data(self):
#         self.client.get("http://localhost:4000/" + self.tyreId)

# class EdgeUser(HttpLocust):
#     task_set = EdgeClient
#     host = "localhost"
#     min_wait = 10000
#     max_wait = 20000

# --------------------------------------------------------------------------- #
# Kafka ingester
# --------------------------------------------------------------------------- #

# ingestion_client_id = 0
# def next_ingestion_client_id():
#   global ingestion_client_id
#   ingestion_client_id += 1
#   return ingestion_client_id

# class IngestionClient(TaskSet):

#     def on_start(self):
#       self.tyreId = str(next_ingestion_client_id())
#       self.pressure = 12.0 + randint(0, 5)

#     @task
#     def send_pressure_update(self):
#         delta = random()
#         if (randint(1, 2) % 2 is 0):
#           self.pressure += delta
#         else:
#           self.pressure -= delta
#         data = json.dumps({"tyreId": self.tyreId, "pressure": self.pressure})
#         self.client.post("http://localhost:7000/ingest", data=data)

# class IngestionUser(HttpLocust):
#     task_set = IngestionClient
#     host = "localhost"
#     min_wait = 5000
#     max_wait = 10000

package main

import (
	"context"
	"encoding/json"
	"log"
	"math/rand/v2"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
)

type Data struct {
	ID             int     `json:"id"`
	Temperature    float64 `json:"temperature"`
	Humidity       float64 `json:"humidity"`
	GenerationDate string  `json:"generationDate"`
}

func main() {
	conn, err := amqp.Dial("amqp://admin:adminpass@localhost:5672/")
	failOnError(err, "Failed to connect to RabbitMQ")
	defer conn.Close()

	ch, err := conn.Channel()
	failOnError(err, "Failed to open a channel")
	defer ch.Close()

	q, err := ch.QueueDeclare(
		"sensor_data",
		true,
		false,
		false,
		false,
		nil,
	)
	failOnError(err, "Failed to declare a queue")

	for {
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)

		data := createMessage()
		body, err := json.Marshal(data)
		failOnError(err, "Failed to marshall JSON")

		sendMessage(q, body, ch, ctx)

		cancel()
		time.Sleep(time.Second)
	}
}

func sendMessage(q amqp.Queue, body []byte, ch *amqp.Channel, ctx context.Context) {
	err := ch.PublishWithContext(ctx,
		"",
		q.Name,
		false,
		false,
		amqp.Publishing{
			ContentType: "application/json",
			Body:        body,
		})
	failOnError(err, "Failed to publish a message")
	log.Printf("Mesage Sent %s\n", body)
}

func createMessage() Data {
	generatedAt := time.Now()
	formattedDate := generatedAt.Format("2006-01-02")

	return Data{
		ID:             rand.IntN(2) + 1,
		Temperature:    10 + rand.Float64()*30,
		Humidity:       10 + rand.Float64()*30,
		GenerationDate: formattedDate,
	}
}

func failOnError(err error, msg string) {
	if err != nil {
		log.Panicf("%s: %s", msg, err)
	}
}

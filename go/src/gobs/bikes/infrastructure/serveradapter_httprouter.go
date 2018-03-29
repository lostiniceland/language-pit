package infrastructure

import (
	"github.com/julienschmidt/httprouter"
	"net/http"
	"encoding/json"
	"log"
	"strconv"
	"fmt"
	"os"
	"net"
	"gobs/bikes/application"
)

type HttpRouterService struct {
	Port string
}


func (service HttpRouterService) ListenAndServe(app application.Application) {
	log.Println("Starting httprouter server")
	r := httprouter.New()

	routeBikes(r, app)
	routeApproval(r, app)

	http.ListenAndServe(service.Port, r)
	log.Println("Stopping httprouter server")
}

func routeBikes(r *httprouter.Router, app application.Application) {

	r.GET("/bikes", func(w http.ResponseWriter, r *http.Request, _ httprouter.Params) {
		var allBikeEntities = app.GetRepository().FindAllBikes()
		body, _ := json.MarshalIndent(convertBikeEntities(allBikeEntities), "", "  ")
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		w.Write(body)
	})

	r.POST("/bikes", func(w http.ResponseWriter, r *http.Request, _ httprouter.Params) {
		decoder := json.NewDecoder(r.Body)
		var postData PostBike
		err := decoder.Decode(&postData)
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			fmt.Fprint(w, err)
		} else {
			bikeP, err := app.CreateBike(postData.Manufacturer, postData.Name, postData.Weight, convertPartResourcesToDTOs(postData.Parts))
			if err != nil {
				w.WriteHeader(http.StatusInternalServerError)
				fmt.Fprint(w, err)
			}
			allBikeEntities := app.GetRepository().FindAllBikes()
			body, _ := json.MarshalIndent(convertBikeEntities(allBikeEntities), "", "\t")
			w.Header().Set("Content-Type", "application/json")
			w.Header().Set("Location", fmt.Sprintf("http://%s:8080/bikes/%d", localhostIp(), bikeP.Id))
			w.WriteHeader(http.StatusCreated)
			w.Write(body)
		}

	})

	r.GET("/bikes/:id", func(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
		id, err := strconv.Atoi(ps.ByName("id"))
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			fmt.Fprint(w, err)
		} else {
			bikeEntity, err := app.GetRepository().FindBike(id)
			if err != nil {
				w.WriteHeader(http.StatusBadRequest)
				fmt.Fprint(w, err)
			} else {
				body, _ := json.MarshalIndent(convertBikeEntity(&bikeEntity), "", "\t")
				w.Header().Set("Content-Type", "application/json")
				w.WriteHeader(http.StatusOK)
				w.Write(body)
			}
		}
	})

	r.PUT("/bikes/:id", func(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
		id, err := strconv.Atoi(ps.ByName("id"))
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			fmt.Fprint(w, err)
		} else {
			decoder := json.NewDecoder(r.Body)
			var resource BikeResource
			err = decoder.Decode(&resource)
			if err != nil {
				w.WriteHeader(http.StatusBadRequest)
				fmt.Fprint(w, err)
			} else {
				bike, err := app.UpdateBike(id, resource.Manufacturer, resource.Name, resource.Weight, convertPartResourcesToDTOs(resource.Parts))
				if err != nil {
					// TODO improve, because some errors are more appropriate for MethodNotAllowed
					w.WriteHeader(http.StatusInternalServerError)
					fmt.Fprint(w, err)
				} else {
					body, _ := json.MarshalIndent(convertBikeEntity(&bike), "", "\t")
					w.Header().Set("Content-Type", "application/json")
					w.Header().Set("Location", fmt.Sprintf("http://%s:8080/bikes/%d", localhostIp(), bike.Id))
					w.WriteHeader(http.StatusOK)
					w.Write(body)
				}
			}
		}

	})
}


func routeApproval(r *httprouter.Router, app application.Application) {

	r.GET("/bikes/:id/approval", func(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
		id, err := strconv.Atoi(ps.ByName("id"))
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			fmt.Fprint(w, err)
		} else {
			bikeEntity, err := app.GetRepository().FindBike(id)
			if err != nil {
				w.WriteHeader(http.StatusBadRequest)
				fmt.Fprint(w, err)
			} else {
				var approvalResource = ApprovalResource{
					BikeId:   bikeEntity.Id,
					Approval: convertStatus(bikeEntity.Approval),
				}
				body, _ := json.MarshalIndent(approvalResource, "", "  ")
				w.Header().Set("Content-Type", "application/json")
				w.WriteHeader(http.StatusOK)
				w.Write(body)

			}
		}
	})

	r.POST("/bikes/:id/approval", func(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
		id, err := strconv.Atoi(ps.ByName("id"))
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			fmt.Fprint(w, err)
		} else {
			decoder := json.NewDecoder(r.Body)
			var resource ApprovalResource
			err = decoder.Decode(&resource)
			if err != nil {
				w.WriteHeader(http.StatusBadRequest)
				fmt.Fprint(w, err)
			} else {
				bikeEntity, err := app.UpdateApproval(id, convertStringStatus(resource.Approval))
				if err != nil {
					w.WriteHeader(http.StatusMethodNotAllowed)
					fmt.Fprint(w, err)
				} else {
					var approvalResource = ApprovalResource{
						BikeId:   bikeEntity.Id,
						Approval: convertStatus(bikeEntity.Approval),
					}
					body, _ := json.MarshalIndent(approvalResource, "", "\t")
					w.Header().Set("Content-Type", "application/json")
					w.WriteHeader(http.StatusOK)
					w.Write(body)
				}
			}
		}
	})
}

func localhostIp() string {
	name, _ := os.Hostname()
	addrs, _ := net.LookupHost(name)
	return addrs[0];
}

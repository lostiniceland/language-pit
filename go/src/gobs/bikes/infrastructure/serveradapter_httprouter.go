package infrastructure

import (
	"github.com/julienschmidt/httprouter"
	"net/http"
	"encoding/json"
	"gobs/bikes/domain"
	"log"
	"strconv"
	"fmt"
	"os"
	"net"
)

type HttpRouterService struct {
	Port string
}

//type dependencyHolder struct {
//	bikeRepository domain.BikeRepository
//	approvalRepository domain.ApprovalRepository
//}


func (service HttpRouterService) ListenAndServe(bikeRepo domain.BikeRepository) {
	log.Println("Starting httprouter server")
	r := httprouter.New()

	routeBikes(r, bikeRepo)
	routeApproval(r, bikeRepo)

	http.ListenAndServe(":" + service.Port, r)
}

func routeBikes(r *httprouter.Router, bikeRepository domain.BikeRepository) {

	r.GET("/bikes", func(w http.ResponseWriter, r *http.Request, _ httprouter.Params) {
		var allBikeEntities = bikeRepository.FindAllBikes()
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
			var newBikeP= domain.NewBike(postData.Manufacturer, postData.Name, postData.Weight, postData.Parts)
			var allBikeEntities= bikeRepository.AddBike(newBikeP)
			body, _ := json.MarshalIndent(convertBikeEntities(allBikeEntities), "", "\t")
			w.Header().Set("Content-Type", "application/json")
			w.Header().Set("Location", fmt.Sprintf("http://%s:8080/bikes/%d", localhostIp(), newBikeP.Id))
			w.WriteHeader(http.StatusCreated)
			w.Write(body)
		}

	})

	r.GET("/bikes/:id", func(w http.ResponseWriter, r *http.Request, ps httprouter.Params){
		id, err := strconv.Atoi(ps.ByName("id"))
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			fmt.Fprint(w, err)
		} else {
			bikeEntity, err := bikeRepository.FindBike(id)

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

	r.PUT("/bikes/:id", func(w http.ResponseWriter, r *http.Request, ps httprouter.Params){
		id, err := strconv.Atoi(ps.ByName("id"))
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			fmt.Fprint(w, err)
		} else {
			bike, err := bikeRepository.FindBike(id)
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
					err = bike.Update(resource.Manufacturer, resource.Name, resource.Weight, resource.Parts)
					if err != nil {
						w.WriteHeader(http.StatusMethodNotAllowed)
						fmt.Fprint(w, err)
					} else {
						err = bikeRepository.SaveBike(&bike)
						if err != nil {
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
			}
		}
	})
}

// try to get rid of duplication
//func getBikeByIdSegment(idSegment string, repository domain.BikeRepository) domain.Bike {
//	id, err := strconv.Atoi(idSegment)
//	if err != nil {
//		w.WriteHeader(http.StatusBadRequest)
//		fmt.Fprint(w, err)
//	} else {
//		bike, err := bikeRepository.FindBike(id)
//	}
//}

func routeApproval(r *httprouter.Router, bikeRepository domain.BikeRepository) {

	r.GET("/bikes/:id/approval", func(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
		id, err := strconv.Atoi(ps.ByName("id"))
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			fmt.Fprint(w, err)
		} else {
			bikeEntity, err := bikeRepository.FindBike(id)
			if err != nil {
				w.WriteHeader(http.StatusBadRequest)
				fmt.Fprint(w, err)
			} else {
				var approvalResource = ApprovalResource{
					BikeId:bikeEntity.Id,
					Approval: convertStatus(bikeEntity.Approval.Status),
				}
				body, _ := json.MarshalIndent(approvalResource, "", "  ")
				w.Header().Set("Content-Type", "application/json")
				w.WriteHeader(http.StatusOK)
				w.Write(body)

			}
		}
	})

	r.POST("/bikes/:id/approval", func(w http.ResponseWriter, r *http.Request, ps httprouter.Params){
		id, err := strconv.Atoi(ps.ByName("id"))
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			fmt.Fprint(w, err)
		} else {
			bikeEntity, err := bikeRepository.FindBike(id)
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
					err = bikeEntity.UpdateApproval(convertStringStatus(resource.Approval))
					if err != nil {
						w.WriteHeader(http.StatusMethodNotAllowed)
						fmt.Fprint(w, err)
					} else {
						var approvalResource = ApprovalResource{
							BikeId:bikeEntity.Id,
							Approval: convertStatus(bikeEntity.Approval.Status),
						}
						body, _ := json.MarshalIndent(approvalResource, "", "\t")
						w.Header().Set("Content-Type", "application/json")
						w.WriteHeader(http.StatusOK)
						w.Write(body)
					}
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

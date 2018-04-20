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
	"github.com/golang/protobuf/proto"
	"io/ioutil"
)

const (
	HeaderAccept      = "Accept"
	HeaderContentType = "Content-Type"
	MediaTypeProtobuf = "application/x-protobuf"
	MediaTypeJson     = "application/json"
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
		var body []byte
		if r.Header.Get(HeaderAccept) == MediaTypeProtobuf {
			body, _ = proto.Marshal(convertBikeEntities(allBikeEntities))
			w.Header().Set(HeaderContentType, MediaTypeProtobuf)
		} else {
			body, _ = json.MarshalIndent(convertBikeEntities(allBikeEntities), "", "  ")
			w.Header().Set(HeaderContentType, MediaTypeJson)
		}
		w.WriteHeader(http.StatusOK)
		w.Write(body)
	})

	r.POST("/bikes", func(w http.ResponseWriter, r *http.Request, _ httprouter.Params) {

		var postData CreateBikeMessage
		var err error
		if r.Header.Get(HeaderContentType) == MediaTypeProtobuf {
			data, _ := ioutil.ReadAll(r.Body)
			err = proto.Unmarshal(data, &postData)
		} else {
			decoder := json.NewDecoder(r.Body)
			err = decoder.Decode(&postData)
		}
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			fmt.Fprint(w, err)
		} else {
			bikeP, err := app.CreateBike(
				postData.Manufacturer,
				postData.Name,
				postData.Weight,
				postData.Value,
				convertPartToDomainPart(postData.Parts))
			if err != nil {
				w.WriteHeader(http.StatusInternalServerError)
				fmt.Fprint(w, err)
			}
			allBikeEntities := app.GetRepository().FindAllBikes()
			body, _ := json.MarshalIndent(convertBikeEntities(allBikeEntities), "", "\t")

			if r.Header.Get(HeaderContentType) == MediaTypeProtobuf {
				body, _ = proto.Marshal(convertBikeEntities(allBikeEntities))
				w.Header().Set(HeaderContentType, MediaTypeProtobuf)
			} else {
				body, _ = json.MarshalIndent(convertBikeEntities(allBikeEntities), "", "  ")
				w.Header().Set(HeaderContentType, MediaTypeJson)
			}
			w.Header().Set("Location", fmt.Sprintf("http://%s:8080/bikes/%d", localhostIp(), bikeP.Id))
			w.WriteHeader(http.StatusCreated)
			w.Write(body)
		}

	})

	r.GET("/bikes/:id", func(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
		id, err := strconv.ParseInt(ps.ByName("id"), 10, 64)
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			fmt.Fprint(w, err)
		} else {
			bikeEntity, err := app.GetRepository().FindBike(id)
			if err != nil {
				w.WriteHeader(http.StatusBadRequest)
				fmt.Fprint(w, err)
			} else {
				var body []byte
				if r.Header.Get(HeaderContentType) == MediaTypeProtobuf {
					body, _ = proto.Marshal(convertBikeEntity(&bikeEntity))
					w.Header().Set(HeaderContentType, MediaTypeProtobuf)
				} else {
					body, _ = json.MarshalIndent(convertBikeEntity(&bikeEntity), "", "  ")
					w.Header().Set(HeaderContentType, MediaTypeJson)
				}
				w.WriteHeader(http.StatusOK)
				w.Write(body)
			}
		}
	})

	r.PUT("/bikes/:id", func(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
		id, err := strconv.ParseInt(ps.ByName("id"), 10, 64)
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			fmt.Fprint(w, err)
		} else {
			var resource BikeMessage
			var err error
			if r.Header.Get(HeaderContentType) == MediaTypeProtobuf {
				data, _ := ioutil.ReadAll(r.Body)
				err = proto.Unmarshal(data, &resource)
			} else {
				decoder := json.NewDecoder(r.Body)
				err = decoder.Decode(&resource)
			}
			if err != nil {
				w.WriteHeader(http.StatusBadRequest)
				fmt.Fprint(w, err)
			} else {
				bike, err := app.UpdateBike(
					id,
					resource.Manufacturer,
					resource.Name,
					resource.Weight,
					resource.Value,
					convertPartToDomainPart(resource.Parts))
				if err != nil {
					// TODO improve, because some errors are more appropriate for MethodNotAllowed
					w.WriteHeader(http.StatusInternalServerError)
					fmt.Fprint(w, err)
				} else {
					var body []byte
					if r.Header.Get(HeaderContentType) == MediaTypeProtobuf {
						body, _ = proto.Marshal(convertBikeEntity(&bike))
						w.Header().Set(HeaderContentType, MediaTypeProtobuf)
					} else {
						body, _ = json.MarshalIndent(convertBikeEntity(&bike), "", "  ")
						w.Header().Set(HeaderContentType, MediaTypeJson)
					}
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
		id, err := strconv.ParseInt(ps.ByName("id"), 10, 64)
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			fmt.Fprint(w, err)
		} else {
			bikeEntity, err := app.GetRepository().FindBike(id)
			if err != nil {
				w.WriteHeader(http.StatusBadRequest)
				fmt.Fprint(w, err)
			} else {
				var approvalResource = ApprovalMessage{
					BikeId:   bikeEntity.Id,
					Approval: convertStatus(bikeEntity.Approval),
				}
				var body []byte
				if r.Header.Get(HeaderAccept) == MediaTypeProtobuf {
					body, _ = proto.Marshal(&approvalResource)
					w.Header().Set(HeaderContentType, MediaTypeProtobuf)
				} else {
					// default is to write JSON
					body, _ = json.MarshalIndent(&approvalResource, "", "  ")
					w.Header().Set(HeaderContentType, MediaTypeJson)
				}
				w.WriteHeader(http.StatusOK)
				w.Write(body)

			}
		}
	})

	r.POST("/bikes/:id/approval", func(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
		id, err := strconv.ParseInt(ps.ByName("id"), 10, 64)
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			fmt.Fprint(w, err)
		} else {
			var resource ApprovalMessage
			var err error
			if r.Header.Get(HeaderContentType) == MediaTypeProtobuf {
				data, _ := ioutil.ReadAll(r.Body)
				err = proto.Unmarshal(data, &resource)
			} else {
				decoder := json.NewDecoder(r.Body)
				err = decoder.Decode(&resource)
			}
			if err != nil {
				w.WriteHeader(http.StatusBadRequest)
				fmt.Fprint(w, err)
			} else {
				bikeEntity, err := app.UpdateApproval(id, convertProtoStatus(resource.Approval))
				if err != nil {
					w.WriteHeader(http.StatusMethodNotAllowed)
					fmt.Fprint(w, err)
				} else {
					var approvalResource = ApprovalMessage{
						BikeId:   bikeEntity.Id,
						Approval: convertStatus(bikeEntity.Approval),
					}
					var body []byte
					if r.Header.Get(HeaderContentType) == MediaTypeProtobuf {
						body, _ = proto.Marshal(&approvalResource)
						w.Header().Set(HeaderContentType, MediaTypeProtobuf)
					} else {
						body, _ = json.MarshalIndent(&approvalResource, "", "  ")
						w.Header().Set(HeaderContentType, MediaTypeJson)
					}
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

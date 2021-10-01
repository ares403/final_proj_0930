![vaccine](https://user-images.githubusercontent.com/50683559/135287230-8f5a4548-49d7-46ae-b901-6645f47dd2fb.png)
https://user-images.githubusercontent.com/50683559/135287230-8f5a4548-49d7-46ae-b901-6645f47dd2fb.png
# 백신

백신을 예약하고 취소하는 기능을 구현한 Microservice

# Table of contents

- [백신](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd-설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출--서킷-브레이킹--장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [Self-Healing](#self-healing)
    - [무정지 재배포](#무정지-재배포)
    - [Persistant Volume Claim](#persistant-volume-claim)
  - [신규 개발 조직의 추가](#신규-개발-조직의-추가)

# 서비스 시나리오

백신 예약 기능 구현하기 

기능적 요구사항
1. 고객은 백신을 예약한다.
2. 백신 물량을 확인한다.   
3. 백신 물량이 있다면 백신이 예약된다.
4. 백신 물량이 없다면 백신이 예약되지 않는다.
5. 백신이 예약되면 물량이 1개 줄어든다.
6. 고객은 백신을 취소한다.
7. 취소한 백신에 해당하는 물량이 1개 늘어난다.

비기능적 요구사항
1. 트랜잭션
    1. 백신의 물량이 없다면 예약이 되지 않아야 한다. - Sync
1. 장애격리
    1. 고객은 백신 물량 상태와 상관없이 예약을 할 수 있다. - Event Driven
    1. 예약 주문이 많으면 물량 확인을 잠시 뒤에 하도록 한다. - Circuit Break
1. 성능
    1. 고객이 예약 상태를 프론트엔드에서 조회할 수 있어야 한다.  CQRS
    1. 예약한 백신의 상태가 바뀔 때마다 고객에게 알림을 줄 수 있어야 한다  Event driven
    


# 체크포인트

- 분석 설계


  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 
  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?


# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)

<img width="1130" alt="2021-09-12 8 55 49" src="https://user-images.githubusercontent.com/89987635/132986657-418ebe58-2158-4f9e-a237-0bf980efb050.png">

## TO-BE 조직 (Vertically-Aligned)

<img width="1093" alt="2021-09-12 11 13 12" src="https://user-images.githubusercontent.com/89987635/132991004-2fdfb1de-977f-4a64-8bf8-34d24f29c7e4.png">


## Event Storming 결과

* MSAEz 로 모델링한 이벤트스토밍 결과: https://labs.msaez.io/#/storming/zm7538qsNkhoDMQ3F0AUMpn1wHS2/8e220fa460d7f3692354e798ad599a22


### 이벤트 도출

<img width="1371" alt="2021-09-12 11 42 52" src="https://user-images.githubusercontent.com/50683559/135305045-5647b36b-eef9-4ba4-a7bc-18bfe42b2a34.png">

### 부적격 이벤트 탈락

<img width="1371" alt="2021-09-12 11 43 07" src="https://user-images.githubusercontent.com/50683559/135305103-ebad3629-c4de-4bd2-8db5-4d35e4190422.png">

- 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
- 요청승인됨 :  예약성공됨과 중복된 의미임으로 제외
- 백신조회됨 : View와 기능이 유사함으로 제외
- 백신입고됨 : 외부 시스템의 이벤트임으로 제외

### 액터, 커맨드 부착하여 읽기 좋게

<img width="1426" alt="2021-09-13 9 28 38" src="https://user-images.githubusercontent.com/50683559/135449744-b42d25b8-b851-4d32-bc29-4f1baca80557.png">

### 어그리게잇으로 묶기

<img width="1500" alt="2021-09-13 10 01 42" src="https://user-images.githubusercontent.com/50683559/135449769-98eb4458-a5fe-4a60-8b79-18accea91865.png">

- Customer의 예약과 취소, Reservation의 백신 요청과 원복, Vaccine의 백신 성공과 실패와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 그들 끼리 묶어줌

### 바운디드 컨텍스트로 묶기

<img width="1475" alt="2021-09-13 10 02 53" src="https://user-images.githubusercontent.com/50683559/135449796-d1df63c1-a1cf-41a3-b69c-4423994fdaa4.png">

    - 도메인 서열 분리 
      - Core Domain:  Reservation, Customer : 없어서는 안될 핵심 서비스이며, 연견 Up-time SLA 수준을 99.999% 목표, 배포주기는 의 경우 1주일 1회 미만
      - Supporting Domain: Vaccine : 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 1주일 1회 이상을 기준으로 함.

### 폴리시 부착 (괄호는 수행주체, 전체 연계가 초기에 드러남)

<img width="1551" alt="2021-09-13 10 47 23" src="https://user-images.githubusercontent.com/50683559/135449827-37486d60-04eb-4e54-833a-4c1e18035c48.png">

### 폴리시의 이동과 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)

<img width="1498" alt="2021-09-13 10 49 08" src="https://user-images.githubusercontent.com/50683559/135449865-6ee03441-3290-48c2-a5c3-9d719ad46cf6.png">

### 완성된 1차 모형

<img width="1469" alt="2021-09-13 11 00 17" src="https://user-images.githubusercontent.com/50683559/135449896-1edbde74-ea95-4a5a-98ce-6e5e675544ef.png">

    - View Model 추가

### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

<img width="1448" alt="2021-09-13 11 11 59" src="https://user-images.githubusercontent.com/50683559/135449951-ce81a497-bb32-4827-b08c-531fed66001b.png">

    - 고객은 백신을 예약한다. (ok)
    - 백신 물량을 확인한다. (ok)
    - 백신 물량이 있다면 백신이 예약된다. (ok) 
    - 백신이 예약되면 물량이 1개 줄어든다. (ok)

<img width="1430" alt="2021-09-13 11 23 30" src="https://user-images.githubusercontent.com/50683559/135449976-fe1d6126-c4e8-4ce0-b291-466ee1bdf1cc.png">

    - 고객은 백신을 예약한다. (ok)
    - 백신 물량을 확인한다. (ok)
    - 백신 물량이 없다면 백신이 예약되지 않는다. (ok)

<img width="1430" alt="2021-09-13 11 16 05" src="https://user-images.githubusercontent.com/50683559/135450013-00a20a1a-8be5-4184-a3f6-63ae9e25bef4.png">

    - 고객은 백신을 취소한다. (ok)
    - 취소한 백신에 해당하는 물량이 1개 늘어난다. (ok)


### 비기능 요구사항에 대한 검증

<img width="1430" alt="2021-09-13 11 33 01" src="https://user-images.githubusercontent.com/50683559/135450052-6747b884-fb16-4309-9442-ee8efee704f3.png">

    - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
        - 백신 물량 확인:  ACID 트랜잭션 적용. 반드시 물량이 있어야지 예약이 가능하므로 Request-Response 방식 처리
        - 나머지 모든 inter-microservice 트랜잭션: 취소, 성공여부 등 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함



## 헥사고날 아키텍처 다이어그램 도출
    
<img width="1481" alt="2021-09-13 11 51 17" src="https://user-images.githubusercontent.com/50683559/135450078-6713c9ee-ce64-4600-80d7-a50245052268.png">


    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8080 ~ 8084 이다)

```
cd gateway
mvn spring-boot:run

cd Customer
mvn spring-boot:run 

cd Reservation
mvn spring-boot:run  

cd Vaccine
mvn spring-boot:run

cd View
mvn spring-boot:run
```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 Reservation 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어(유비쿼터스 랭귀지)를 영어로 번역하여 사용하였다. 

```
package vaccine;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String reserveStatus;
    private Long customerId;
    private Long vaccineId;
    private String customerName;

    @PostPersist
    public void onPostPersist() {
        if (this.reserveStatus.equals("OK")) {
            VaccineRequested vaccineRequested = new VaccineRequested();
            BeanUtils.copyProperties(this, vaccineRequested);
            vaccineRequested.publishAfterCommit();

            vaccineRequested.saveJasonToPvc(vaccineRequested.toJson());
        }
    }

    @PostUpdate
    public void onPostUpdate(){
        if (this.reserveStatus.equals("NO")) {
            VaccineBacked vaccineBacked = new VaccineBacked();
            BeanUtils.copyProperties(this, vaccineBacked);
            vaccineBacked.publishAfterCommit();

            vaccineBacked.saveJasonToPvc(vaccineBacked.toJson());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getReserveStatus() {
        return reserveStatus;
    }

    public void setReserveStatus(String reserveStatus) {
        this.reserveStatus = reserveStatus;
    }
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    public Long getVaccineId() {
        return vaccineId;
    }

    public void setVaccineId(Long vaccineId) {
        this.vaccineId = vaccineId;
    }
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

}

```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```

package vaccine;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="reservations", path="reservations")
public interface ReservationRepository extends PagingAndSortingRepository<Reservation, Long>{

    Reservation findByCustomerId(Long customerId);
    
}

```
- 적용 후 REST API 의 테스트
```

# 고객과 백신 등록
http POST localhost:8081/customers name="bs" vaccineId=1 reserveStatus="OK"
http POST localhost:8083/vaccines name="xxx" qty=100 status="OK"

# 확인
http http://localhost:8081/customers/1
http http://localhost:8083/vaccines/1


# 백신예약
http POST http://localhost:8081/customer/reserve id=2 name="bs" vaccineId=1

# 예약 확인
http http://localhost:8082/reservations

```

## 폴리글랏 퍼시스턴스

전체 서비스의 경우 빠른 속도와 개발 생산성을 극대화하기 위해 Spring Boot에서 기본적으로 제공하는 In-Memory DB인 H2 DB를 사용하였다.

```
# Customer.java

package vaccine;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Customer_table")
public class Customer {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String name;
    private Long vaccineId;
    private String vaccineName;
    private String reserveStatus;
    

# application.yml

  h2:
    console:
      enabled: true
      path: /h2-console
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb
    username: sa
    password:         

# pom.xml
  <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <scope>runtime</scope>
  </dependency>
    
```

## 폴리글랏 프로그래밍

View 서비스(dashboard)의 경우 다른 서비스와 다르게 HSQL DB를 사용하였다.

```

# application.yml

  datasource:
    driver-class-name: org.hsqldb.jdbc.JDBCDriver
    url: jdbc:hsqldb:mem:testdb
    username: sa
    password:
    
# pom.xml
    <dependency>
        <groupId>org.hsqldb</groupId>
        <artifactId>hsqldb</artifactId>
        <scope>runtime</scope>
    </dependency>

```


## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 고객(customer)->예약(reservation) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 결제 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```

# (ReservationService.java) 

package vaccine.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name="reservation", url="${api.url.reservation}", fallback = ReservationServiceImpl.class)
public interface ReservationService {
    @RequestMapping(method= RequestMethod.POST, path="/reserve")
    public boolean reserve(@RequestBody Reservation reservation);

}

# applicaytion.yml
api:
  url:
    reservation: http://localhost:8082/reservation

```

- 예약 직후(@PostPersist) 결제를 요청하도록 처리
```

# Reservation.java (Entity)

  @PostPersist
  public void onPostPersist() {

    convenience.store.external.PayHistory payHistory = new convenience.store.external.PayHistory();

    payHistory.setPayStatus(this.status);
    payHistory.setReserveStatus("RESERVE");
    payHistory.setReserveId(this.id);
    payHistory.setCustomerId(this.customerId);
    payHistory.setCustomerName(this.customerName);
    payHistory.setCustomerPhone(this.customerPhone);
    payHistory.setDate(this.date);
    payHistory.setReserveDate(this.date);
    payHistory.setProductId(this.productId);
    payHistory.setProductPrice(this.productPrice);
    payHistory.setReserveQty(this.qty);

    boolean result = ReservationApplication.applicationContext.getBean(convenience.store.external.PayHistoryService.class).request(payHistory);

  }

```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 예약도 불가하다는 것을 확인:


```
# 예약 (Pay) 서비스 종료

# 예약처리

http POST http://localhost:8081/customer/reserve id=2 name="bs" vaccineId=1   #Fail

# 예약 (Pay) 서비스 재시작

# 예약처리
http POST http://localhost:8081/customer/reserve id=2 name="bs" vaccineId=1   #Success


```

- 또한 과도한 예약 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)


## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

예약이 이루어지면 백신에 비동기식으로 정보를 전달해주기로 했다
- 이를 위하여 도메인 이벤트를 카프카로 송출한다. (Publish)
  이때 다른 저장 로직에 의해서 해당 이벤트가 발송되는 것을 방지하기 위해 Status 체크하는 로직을 추가했다.
 
```

    @PostPersist
    public void onPostPersist() {
        if (this.reserveStatus.equals("OK")) {
            VaccineRequested vaccineRequested = new VaccineRequested();
            BeanUtils.copyProperties(this, vaccineRequested);
            vaccineRequested.publishAfterCommit();
    
            vaccineRequested.saveJasonToPvc1(vaccineRequested.toJson());
        }
    }

    @PostUpdate
    public void onPostUpdate(){
        if (this.reserveStatus.equals("NO")) {
            VaccineBacked vaccineBacked = new VaccineBacked();
            BeanUtils.copyProperties(this, vaccineBacked);
            vaccineBacked.publishAfterCommit();
    
            vaccineBacked.saveJasonToPvc2(vaccineBacked.toJson());
        }
    }

```
- 백신 서비스에서는 예약이 들어오면 백신의 갯수를 하나 줄이고 그 값이 0보다 크면 성공 작으면 실패로 상태를 업데이트 해준다.

```
package convenience.store;

...

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverVaccineRequested_CheckVaccine(@Payload VaccineRequested vaccineRequested){

        if(!vaccineRequested.validate()) return;

        System.out.println("\n\n##### listener CheckVaccine : " + vaccineRequested.toJson() + "\n\n");

        // Sample Logic //
        Vaccine vaccine = vaccineRepository.findById(vaccineRequested.getVaccineId()).orElseThrow(null);
        vaccine.setQty(vaccine.getQty()-1);

        if(vaccine.getQty()>=0) {
            vaccine.setStatus("OK");
        }
        else{
            vaccine.setStatus("NO");
        }
        vaccineRepository.save(vaccine);

    }

```
실제 구현을 하자면, 결제가 완료된 이후에 카톡 알림을 통해 예약이 완료되었다는 외부 이벤트를 보내고, 점주는 예약 상태를 Dashboard를 통해 확인할 수 있다.
  
```

# 예약 현황을 Dashboard에서 확인

http http://localhost:8084/dashboards

```


# 운영

## CI/CD 설정
각 구현체들은 각자의 AWS의 ECR 에 구성되었고, 사용한 CI/CD 플랫폼은 AWS-CodeBuild를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하에 buildspec-kubectl.yaml 에 포함되었다.

- 레포지터리 생성 확인
  - 이미지 변경 필요.

![image](https://user-images.githubusercontent.com/22004206/132270281-d9f0154e-ba48-442f-90f2-9208b6d1886e.png)

<br/>

- 생성 할 CodeBuild
  - convenience-gateway
  - convenience-reservation
  - convenience-pay
  - convenience-store
  - convenience-stock
  - convenience-view
<br/>


- github의 각 서비스의 서브 폴더에 buildspec-kubect.yaml 위치.

![image](https://user-images.githubusercontent.com/22004206/133250463-b7c80d2c-e58b-4329-8ded-dca2b146215a.png)
![image](https://user-images.githubusercontent.com/22004206/133250705-66c3e747-e3aa-4aa5-90a0-1e9efb4210c5.png)
![image](https://user-images.githubusercontent.com/22004206/133250824-3e9689f6-2327-45dd-8322-bacad102e1d3.png)
![image](https://user-images.githubusercontent.com/22004206/133250923-f62f98bb-28bb-4dea-ab6f-6b6b9081c9c1.png)
![image](https://user-images.githubusercontent.com/22004206/133251040-94926311-83d1-422e-95d9-7a8950227966.png)


- 연결된 github에 Commit 진행시 6개의 서비스들 build 진행 여부 및 성공 확인 

![image](https://user-images.githubusercontent.com/22004206/133251313-c2df253e-0b98-4234-84a2-c829ab39a829.png)

![image](https://user-images.githubusercontent.com/22004206/133251727-70c8ce0e-edb7-46bd-8876-6d242e29b05a.png)


-	배포된 6개의 Service  확인
```
> kubectl get all

NAME                          READY   STATUS    RESTARTS   AGE
gateway-6bdf6cf865-n4b8v      1/1     Running   0          15m
pay-5bdf5998d9-qpdtk          1/1     Running   0          14m
reservation-c544fd6bd-47sm5   1/1     Running   0          13m
siege-75d5587bf6-8xnmc        1/1     Running   0          93m
store-546b7cd7c8-gghdv        1/1     Running   0          15m
supplier-6477564dd4-tq9tt     1/1     Running   0          14m    
```




## 동기식 호출 / 서킷 브레이킹 / 장애격리
- 시나리오
  1. 예약(reservation) --> 결재(pay)시의 연결을 RESTful Request/Response 로 연동하여 구현 함. 결제 요청이 과도할 경우 CB가 발생하고 fallback으로 결재 지연 메새지를 보여줌으로 장애 격리 시킴.
  2. circuit break의 timeout은 610mm 설정. 
  3. Pay 서비스에 임의의 부하 처리.
  4. 부하테스터(seige) 를 통한 circuit break 확인. 
    - 결재 지연 메세지 확인.
    - seige의 Availability 100% 확인.

<br/>
    
- 서킷 브레이킹 프레임워크의 선택
  - Spring FeignClient + Hystrix 옵션을 사용하여 구현함

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히고 결재 로직 대신 fallback으로 결재 지연 메세지 보여줌으로 장애 격리.
```
feign:
  hystrix:
    enabled: true

hystrix:
  command:
    # 전역설정 timeout이 610ms 가 넘으면 CB 처리.
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610
```
- Pay 서비스에 임의 부하 처리 - 400 밀리에서 증감 220 밀리 정도 왔다갔다 하게
  - PayHistoryController.java에 아래 코드 추가
```
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
```
- Resevation 서비스에 FeignClient fallback 추가
  - PayHistoryService.java에 아래 코드 추가.
```
@FeignClient(name ="delivery", url="${api.url.pay}", fallback = PayHistoryServiceImpl.class)
```
- - PayHistoryServiceImple.java에 아래 코드 추가.
```
@Service
public class PayHistoryServiceImpl implements PayHistoryService {
    /**
     * Pay fallback
     */
    public boolean request(PayHistory payhistory) {
        System.out.println("@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@");
        System.out.println("@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@");
        System.out.println("@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@");
        return false;
    }
}
```

- 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
  - 동시사용자 100명, 60초 동안 실시
  - Reservation 서비스의 log 확인.
```
> siege -c100 -t60S --content-type "application/json" 'http://reservation:8080/reservation/order POST {"productId":1,"productName":"Milk","productPrice":1200,"customerId":2,"customerName":"Sam","customerPhone":"010-9837-0279","qty":2}'

** SIEGE 4.1.1
** Preparing 100 concurrent users for battle.
The server is now under siege...
HTTP/1.1 201     2.19 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.20 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.20 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.20 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.20 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.21 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.21 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.21 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.22 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.22 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
HTTP/1.1 201     2.66 secs:     378 bytes ==> POST http://reservation:8080/reservation/order
                                        
                                        :
                                        :
                                        :

*Lifting the server siege...
Transactions:		        8776 hits
Availability:		      100.00 %
Elapsed time:		       29.83 secs
Data transferred:	        1.67 MB
Response time:		        0.34 secs
Transaction rate:	      294.20 trans/sec
Throughput:		        0.06 MB/sec
Concurrency:		       99.32
Successful transactions:        8776
Failed transactions:	           0
Longest transaction:	        2.24
Shortest transaction:	        0.00

```
- 결재 서비스에 지연이 발생하는 경우 결재지연 메세지를 보여주고 장애에 분리되어 Avalablity가 100% 이다. 

- 예약 서비스(reservation)의 log에 아래에서 결재 지연 메세지를 확인한다.
```
              :
              :
@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@
@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@
@@@@@@@ 결재 지연중 입니다. @@@@@@@@@@@@
########## 결제가 실패하였습니다 ############
              :
              :
```

- 시스템은 죽지 않고 지속적으로 과도한 부하시 CB 에 의하여 회로가 닫히고 결재 지연중 메세지를 보여주며 고객을 장애로 부터 격리시킴.


## 오토스케일 아웃
- 예약서비스(Reservation)에 대해  CPU Load 50%를 넘어서면 Replica를 10까지 늘려준다. 
  - buildspec-kubectl.yaml
```
          cat <<EOF | kubectl apply -f -
          apiVersion: autoscaling/v2beta2
          kind: HorizontalPodAutoscaler
          metadata:
            name: reservation-hpa
          spec:
            scaleTargetRef:
              apiVersion: apps/v1
              kind: Deployment
              name: $_POD_NAME
            minReplicas: 1
            maxReplicas: 10
            metrics:
            - type: Resource
              resource:
                name: cpu
                target:
                  type: Utilization
                  averageUtilization: 50
          EOF
```

- 예약서비스(reservation)에 대한 CPU Resouce를 1000m으로 제한 한다.
  - buildspec-kubectl.yaml
```
                    resources:
                      limits:
                        cpu: 1000m
                        memory: 500Mi
                      requests:
                        cpu: 500m
                        memory: 300Mi
```

- Siege (로더제너레이터)를 설치하고 해당 컨테이너로 접속한다.
```
> kubectl create deploy siege --image=ghcr.io/acmexii/siege-nginx:latest
> kubectl exec pod/[SIEGE-POD객체] -it -- /bin/bash
```

- 예약 서비스(reseravation)에 워크로드를 동시 사용자 100명 60초 동안 진행한다.
```
siege -c100 -t60S --content-type "application/json" 'http://reservation:8080/reservation/order POST {"productId":1,"productName":"Milk","productPrice":1200,"customerId":2,"customerName":"Sam","customerPhone":"010-9837-0279","qty":2}'
```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다 : 각각의 Terminal에 
  - 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다.
  
```
> kubectl get deploy reservation -w

NAME          READY   UP-TO-DATE   AVAILABLE   AGE
reservation   1/1     1            1           63m
reservation   1/3     1            1           63m
reservation   1/3     1            1           63m
reservation   1/3     1            1           63m
reservation   1/3     3            1           63m
:


> watch -n 1 kubectl top po
NAME                                 READY   STATUS    RESTARTS   AGE   IP               NODE                                              NOMINATED NODE   READINESS GATES
pod/efs-provisioner-77c568c8-pmkxc   1/1     Running   0          16h   192.168.13.208   ip-192-168-5-42.ca-central-1.compute.internal     <none>           <none>
pod/gateway-564d85fbc4-dbhht         1/1     Running   0          70m   192.168.19.153   ip-192-168-5-42.ca-central-1.compute.internal     <none>           <none>
pod/pay-666cf5c795-blfqk             1/1     Running   0          31m   192.168.32.153   ip-192-168-61-25.ca-central-1.compute.internal    <none>           <none>
pod/reservation-779f5585bc-6bdxg     1/1     Running   0          31m   192.168.28.44    ip-192-168-5-42.ca-central-1.compute.internal     <none>           <none>
pod/reservation-779f5585bc-hgjl9     0/1     Running   0          37s   192.168.52.66    ip-192-168-61-25.ca-central-1.compute.internal    <none>           <none>
pod/reservation-779f5585bc-rshlh     0/1     Running   0          37s   192.168.95.48    ip-192-168-73-205.ca-central-1.compute.internal   <none>           <none>
pod/siege-pvc                        1/1     Running   0          16h   192.168.1.22     ip-192-168-20-33.ca-central-1.compute.internal    <none>           <none>


> watch -n 1 kubectl get all -o wide 
NAME                             CPU(cores)   MEMORY(bytes)
efs-provisioner-77c568c8-pmkxc   1m           10Mi
gateway-564d85fbc4-dbhht         7m           150Mi
pay-666cf5c795-blfqk             6m           254Mi
reservation-779f5585bc-6bdxg     4m           280Mi
reservation-779f5585bc-hgjl9     487m         154Mi
reservation-779f5585bc-rshlh     483m         159Mi
siege-pvc                        0m           6Mi
store-7f9f99dbfc-tfsvr           5m           258Mi
supplier-696bb6f7dd-xdpkc        5m           262Mi
view-bdf94d47d-shvwc             4m           279Mi

	
> kubectl get hpa
NAME              REFERENCE                TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
reservation-hpa   Deployment/reservation   1%/50%    1         10        6          82m	
```
<br/>
	
## Self Healing
### ◆ Liveness- HTTP Probe
- 시나리오
  1. Reservation 서비스의 Liveness 설정을 확인힌다. 
  2. Reservation 서비스의 Liveness Probe는 actuator의 health 상태 확인을 설정되어 있어 actuator/health 확인.
  3. pod의 상태 모니터링
  4. Reservation 서비스의 Liveness Probe인 actuator를 down 시켜 Reservation 서비스가 termination 되고 restart 되는 self healing을 확인한다. 
  5. Reservation 서비스의 describe를 확인하여 Restart가 되는 부분을 확인한다.

<br/>

- Reservation 서비스의 Liveness probe 설정 확인
```
kubectl get deploy reservation -o yaml

                  :
        livenessProbe:
          failureThreshold: 5
          httpGet:
            path: /actuator/health
            port: 8080
            scheme: HTTP
          initialDelaySeconds: 120
          periodSeconds: 5
          successThreshold: 1
          timeoutSeconds: 2
                  :
```

- Httpie를 사용하기 위해 Siege를 설치하고 해당 컨테이너로 접속한다.
```
> kubectl create deploy siege --image=ghcr.io/acmexii/siege-nginx:latest
> kubectl exec pod/[SIEGE-POD객체] -it -- /bin/bash
```

- Liveness Probe 확인 
```
> http http://reservation:8080/actuator/health      # Liveness Probe 확인

HTTP/1.1 200 
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Tue, 07 Sep 2021 14:58:15 GMT
Transfer-Encoding: chunked

{
    "status": "UP"
}
```

- Liveness Probe Fail 설정 및 확인 
  - Reservation Liveness Probe를 명시적으로 Fail 상태로 전환한다.
```
> http DELETE http://reservation:8080/healthcheck    #actuator health 를 DOWN 시킨다.
> http http://reservation:8080/actuator/health
HTTP/1.1 503 
Connection: close
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Wed, 08 Sep 2021 01:56:07 GMT
Transfer-Encoding: chunked

{
    "status": "DOWN"
}
```

- Probe Fail에 따른 쿠버네티스 동작확인  
  - Reservation 서비스의 Liveness Probe가 /actuator/health의 상태가 DOWN이 된 것을 보고 restart를 진행함. 
    - reservation pod의 RESTARTS가 1로 바뀐것을 확인. 
    - describe 를 통해 해당 pod가 restart 된 것을 알 수 있다.
```
> kubectl get pod
NAME                          READY   STATUS    RESTARTS
gateway-5587878c8c-7rhx8      1/1     Running   0          8m26s
pay-657d6ff8f5-wvmxs          1/1     Running   0          8m24s
reservation-dc4ff786c-bxp6m   1/1     Running   1          8m23s
siege-75d5587bf6-8xnmc        1/1     Running   0          6m31s
store-6486b7565b-txjjr        1/1     Running   0          8m23s
supplier-9bc6bc8b5-m4l8m      1/1     Running   0          8m23s



> kubectl describe pod/reservation-dc4ff786c-bxp6m
Events:
  Type     Reason     Age                  From               Message
  ----     ------     ----                 ----               -------
  Normal   Scheduled  21m                  default-scheduler  Successfully assigned default/reservation-dc4ff786c-bxp6m to ip-192-168-50-127.ca-central-1.compute.internal
  Normal   Pulling    21m                  kubelet            Pulling image "422489764856.dkr.ecr.ca-central-1.amazonaws.com/user-dongjin-reservation:6a6573b58027490f3d56be72e85d445d6da87746"
  Normal   Pulled     21m                  kubelet            Successfully pulled image "422489764856.dkr.ecr.ca-central-1.amazonaws.com/user-dongjin-reservation:6a6573b58027490f3d56be72e85d445d6da87746" in 1.323451813s
  Normal   Killing    15m                  kubelet            Container reservation failed liveness probe, will be restarted
  Normal   Created    15m (x2 over 21m)    kubelet            Created container reservation
  Normal   Started    15m (x2 over 21m)    kubelet            Started container reservation
  Normal   Pulled     15m                  kubelet            Container image "422489764856.dkr.ecr.ca-central-1.amazonaws.com/user-dongjin-reservation:6a6573b58027490f3d56be72e85d445d6da87746" already present on machine
  Warning  Unhealthy  14m (x4 over 21m)    kubelet            Readiness probe failed: Get "http://192.168.37.58:8080/actuator/health": dial tcp 192.168.37.58:8080: connect: connection refused
  Warning  Unhealthy  4m41s (x8 over 15m)  kubelet            Liveness probe failed: HTTP probe failed with statuscode: 503
  Warning  Unhealthy  4m36s (x8 over 15m)  kubelet            Readiness probe failed: HTTP probe failed with statuscode: 503
```

	
## 무정지 재배포
### ◆ Rediness- HTTP Probe
- 시나리오
  1. 현재 구동중인 Reservation 서비스에 길게(3분) 부하를 준다. 
  2. reservation pod의 상태 모니터링
  3. AWS에 CodeBuild에 연결 되어있는 github의 코드를 commit한다.
  4. Codebuild를 통해 새로운 버전의 Reservation이 배포 된다. 
  5. pod 상태 모니터링에서 기존 Reservation 서비스가 Terminating 되고 새로운 Reservation 서비스가 Running하는 것을 확인한다.
  6. Readness에 의해서 새로운 서비스가 정상 동작할때까지 이전 버전의 서비스가 동작하여 seieg의 Avality가 100%가 된다.

<br/>

- reservstion 서비스의 Readness probe  설정 확인
  - buildspec_kubectl.yaml
```
                    readinessProbe:
                      httpGet:
                        path: /actuator/health
                        port: 8080
                      initialDelaySeconds: 10
                      timeoutSeconds: 2
                      periodSeconds: 5
                      failureThreshold: 10
```

- 현재 구동중인 Reservation 서비스에 길게(3분) 부하를 준다. 
```
> siege -v -c1 -t120S --content-type "application/json" 'http://reservation:8080/reservation/order POST {"productId":1,"productName":"Milk","productPrice":1200,"customerId":2,"customerName":"Sam","customerPhone":"010-9837-0279","qty":2}'
```

- pod의 상태 모니터링
```
> watch -n 1 kubectl get pod    ==> pod가 생성되고 소멸되는 과정 확인.

NAME                          READY   STATUS    RESTARTS   AGE
gateway-6bdf6cf865-n4b8v      1/1     Running   0          15m
pay-5bdf5998d9-qpdtk          1/1     Running   0          14m
reservation-c544fd6bd-47sm5   1/1     Running   0          13m
siege-75d5587bf6-8xnmc        1/1     Running   0          93m
store-546b7cd7c8-gghdv        1/1     Running   0          15m
supplier-6477564dd4-tq9tt     1/1     Running   0          14m    
```

- AWS에 CodeBuild에 연결 되어있는 github의 코드를 commit한다.
  Resevatio 서비스의 아무 코드나 수정하고 commit 한다. 
  배포 될때까지 잠시 기다린다. 
  Ex) buildspec-kubectl.yaml에 carrage return을  추가 commit 한다. 



- pod 상태 모니터링에서 기존 Reservation 서비스가 Terminating 되고 새로운 Reservation 서비스가 Running하는 것을 확인한다.
```
Every 1.0s: kubectl get pod   

NAME                           READY   STATUS    RESTARTS   AGE
gateway-5c7f47c9c5-z5slx       0/1     Running   0          11s
gateway-6bdf6cf865-n4b8v       1/1     Running   0          20m
pay-5bdf5998d9-qpdtk           1/1     Running   0          19m
pay-797f74998c-wh94q           0/1     Running   0          9s
reservation-585667dc8c-wlmtb   0/1     Running   0          8s
reservation-c544fd6bd-47sm5    1/1     Running   0          18m
siege-75d5587bf6-8xnmc         1/1     Running   0          98m
store-546b7cd7c8-gghdv         1/1     Running   0          20m
store-774c6757bd-gh5hx         0/1     Running   0          10s
supplier-6477564dd4-tq9tt      1/1     Running   0          19m
supplier-7bc4ff789d-qgkwk      0/1     Running   0          9s
```

- Readness에 의해서 새로운 서비스가 정상 동작할때까지 이전 버전의 서비스가 동작하여 seieg의 Avalabilty가 100%가 된다.
```
Lifting the server siege...
Transactions:		       18572 hits
Availability:		      100.00 %
Elapsed time:		      119.79 secs
Data transferred:	        6.62 MB
Response time:		        0.01 secs
Transaction rate:	      155.04 trans/sec
Throughput:		        0.06 MB/sec
Concurrency:		        0.95
Successful transactions:       18572
Failed transactions:	           0
Longest transaction:	        0.68
Shortest transaction:	        0.00
```

## Persistant Volume Claim
- 시나리오
  1. EFS 생성 화면 캡쳐.
  2. 등록된 provisoner / storageclass / pvc 확인.
  3. 각 서비스의 buildspec_kubectl.yaml에 pvc 생성 정보 확인.
  4. bash shell을 사용할 수 있는 pod를 동일한 PVC 사용할 수 있게 설정 후 배포하여 '/mnt/aws'에 각 서비스에서 생성한 파일을 확인. 
  

- EFS 등록 화면 추가..
```
이미지 추가.
```

- provisioner 확인
```
> kubectl get pod

NAME                              READY   STATUS    RESTARTS   AGE
efs-provisioner-5976978f5-cqbzq   1/1     Running   0          19s
```

- storageClass 등록, 조회
```
> kubectl get sc
NAME            PROVISIONER             RECLAIMPOLICY   VOLUMEBINDINGMODE      ALLOWVOLUMEEXPANSION   AGE
aws-efs         my-aws.com/aws-efs      Delete          Immediate              false                  14s
gp2 (default)   kubernetes.io/aws-ebs   Delete          WaitForFirstConsumer   false                  27h
```
- pvc 확인
```
> kubectl get pvc
> kubectl describe pvc
  Type    Reason                 Age                From                                                                                     Message
  ----    ------                 ----               ----                                                                                     -------
  Normal  ExternalProvisioning   35s (x2 over 35s)  persistentvolume-controller                                                              waiting for a volume to be created, either by external provisioner "my-aws.com/aws-efs" or manually created by system administrator
  Normal  Provisioning           35s                my-aws.com/aws-efs_efs-provisioner-5976978f5-cqbzq_5cde0b7c-906d-477e-9e02-5b4823a9ca5c  External provisioner is provisioning volume for claim "default/aws-efs"
  Normal  ProvisioningSucceeded  35s                my-aws.com/aws-efs_efs-provisioner-5976978f5-cqbzq_5cde0b7c-906d-477e-9e02-5b4823a9ca5c  Successfully provisioned volume pvc-c770d8b7-ef09-4a19-903b-cced4daa9f1d
```
<br/>

- 각 Deployment의 PVC 생성정보는 buildspec-kubeclt.yaml에 적용되어있다.
```
                    volumeMounts:
                      - mountPath: "/mnt/aws"
                        name: volume
                        :
                        :
                        :
                volumes:
                  - name: volume
                    persistentVolumeClaim:
                      claimName: aws-efs
```


- 각 서비스의 Event 발생시 JSON 정보를 파일로 저장한다. 마지막 정보만 저정하기 위해 Overwirte하여 저장한다. 
  - 아래와 같은 코드를 통하여 /mnt/aws의 경로에 파일을 저장한다. 
```
AbstractEvent.java

    // PVC Test
    public void saveJasonToPvc(String strJson){
        File file = new File("/mnt/aws/xxxxxxxxed_json.txt");

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(strJson);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
```

- 각 서비스에서 저장한 Event 정보파일을 동일한 PVC를 사용하는 Pod를 생성하여 배포 후 /mnt/aws에 저장되어 있는지 확인. 
```
> kubectl apply -f kubectl apply -f https://raw.githubusercontent.com/djjoung/convenience/main/yaml/pod-with-pvc.yaml
> kubectl get pod
> kubectl describe pod reservation
> kubectl exec -it seieg -- /bin/bash
> ls -al /mnt/aws

total 20
drwxrws--x 2 root 2000 6144 Sep 15 14:39 .
drwxr-xr-x 1 root root   17 Sep 15 12:33 ..
-rw-r--r-- 1 root 2000  154 Sep 15 14:37 payCancelled_json.txt
-rw-r--r-- 1 root 2000   99 Sep 15 14:29 productDelivered_json.txt
-rw-r--r-- 1 root 2000  158 Sep 15 14:36 productPickedupjson.txt
-rw-r--r-- 1 root 2000   90 Sep 15 14:37 productReserved_json.txt

```
- 서비스 Event를 저장한 파일들을 확인 할 수 있다. 




<br/>


# 신규 개발 조직의 추가

  ![image](https://user-images.githubusercontent.com/487999/79684133-1d6c4300-826a-11ea-94a2-602e61814ebf.png)


## 마케팅팀의 추가
    - KPI: 신규 고객의 유입률 증대와 기존 고객의 충성도 향상
    - 구현계획 마이크로 서비스: 기존 customer 마이크로 서비스를 인수하며, 고객에 음식 및 맛집 추천 서비스 등을 제공할 예정

## 이벤트 스토밍 
    ![image](https://user-images.githubusercontent.com/487999/79685356-2b729180-8273-11ea-9361-a434065f2249.png)


## 헥사고날 아키텍처 변화 

![image](https://user-images.githubusercontent.com/487999/79685243-1d704100-8272-11ea-8ef6-f4869c509996.png)

## 구현  

기존의 마이크로 서비스에 수정을 발생시키지 않도록 Inbund 요청을 REST 가 아닌 Event 를 Subscribe 하는 방식으로 구현. 기존 마이크로 서비스에 대하여 아키텍처나 기존 마이크로 서비스들의 데이터베이스 구조와 관계없이 추가됨. 

## 운영과 Retirement

Request/Response 방식으로 구현하지 않았기 때문에 서비스가 더이상 불필요해져도 Deployment 에서 제거되면 기존 마이크로 서비스에 어떤 영향도 주지 않음.

* [비교] 결제 (pay) 마이크로서비스의 경우 API 변화나 Retire 시에 app(주문) 마이크로 서비스의 변경을 초래함:

예) API 변화시
```
# Order.java (Entity)

    @PostPersist
    public void onPostPersist(){

        fooddelivery.external.결제이력 pay = new fooddelivery.external.결제이력();
        pay.setOrderId(getOrderId());
        
        Application.applicationContext.getBean(fooddelivery.external.결제이력Service.class)
                .결제(pay);

                --> 

        Application.applicationContext.getBean(fooddelivery.external.결제이력Service.class)
                .결제2(pay);

    }
```

예) Retire 시
```
# Order.java (Entity)

    @PostPersist
    public void onPostPersist(){

        /**
        fooddelivery.external.결제이력 pay = new fooddelivery.external.결제이력();
        pay.setOrderId(getOrderId());
        
        Application.applicationContext.getBean(fooddelivery.external.결제이력Service.class)
                .결제(pay);

        **/
    }
```

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
    1. 고객은 언제든지 자신의 예약 상태를 확인할 수 있다. - Event Driven
    1. 예약 주문이 많으면 잠시 뒤에 하도록 한다. - Circuit Break
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
고객은 예약 상태를 Dashboard를 통해 확인할 수 있다.
  
```

# 예약 현황을 Dashboard에서 확인

http http://localhost:8084/dashboards

```

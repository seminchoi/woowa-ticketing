# 우아한 티켓팅

## 프로젝트 소개

특정 시간에 트래픽이 몰리는 티켓팅 시스템을 구현해보고자 하였습니다.  
병목이 발생할 수 있는 구간에 다양한 구현 방식을 적용하고 실험하며 최적의 방식을 찾습니다.  
최종적으로는 다양한 구현 방식 중 가장 높은 안정성과 처리량을 가지는 기술을 선택하였습니다.

추가적으로 기존 티켓팅 시스템에서 느꼈던 불편점을 개선해보고자 하는 시도를 하였습니다.

## 핵심 기능
### 티켓팅 기능
1. 짧은 시간에 좌석을 선점하기 위해 몰리는 트래픽에서의 동시성을 제어합니다.
2. 더 나은 사용자 경험을 위해 실시간 좌석 선점 상황을 표시합니다. 
   - "이미 예약된 좌석입니다" 문제로 인해 티켓팅이 실패하는 부정적 경험을 개선합니다.  
### 대기열 기능 
- 새로고침해도 순서가 유지되는 대기열 기능을 구현합니다.
  - 다양한 이유로 발생하는 일시적인 대기열 페이지 이탈 시에도 대기열을 일정 시간 유지할 수 있도록 합니다.

## 팀원 소개
<table>
    <tr align="center">
        <td><B>Backend</B></td>
        <td><B>Backend</B></td>
        <td><B>Backend</B></td>
        <td><B>Backend</B></td>
    </tr>
    <tr align="center">
        <td><a href="https://github.com/seminchoi">최세민</a></td>
        <td><a href="https://github.com/mirageoasis">김현우</a></td>
        <td><a href="https://github.com/hseong3243">박혜성</a></td>
        <td><a href="https://github.com/lass9436">이영민</a></td>
    </tr>
    <tr align="center">
        <td>
            <img src="https://github.com/seminchoi.png?size=100">
        </td>
        <td>
            <img src="https://github.com/mirageoasis.png" width="100">
        </td>
        <td>
            <img src="https://github.com/hseong3243.png?size=130">
        </td>
        <td>
            <img src="https://github.com/lass9436.png?size=100">
        </td>
    </tr>
    <tr>
        <td>-인프라 구성</br>-티켓팅 구현</br>-부하 테스트</td>
        <td>-티켓팅 기능 구현</br>-모니터링 환경 및</br>-부하 테스트 환경 구성</td>
        <td>-대기열 구현</br>-인증 구현</br>-부하 테스트 환경 구성</br>-부하 테스트</td>
        <td>-대기열 기능 구현</br>-화면 구현</td>
    </tr>
</table>

## 기술 스택

<div> 
  <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
  <img src="https://img.shields.io/badge/redis-FF4438?style=for-the-badge&logo=redis&logoColor=white">
  <br/>

  <img src="https://img.shields.io/badge/locust-3ECC5F?style=for-the-badge&logo=locust&logoColor=white">
  <img src="https://img.shields.io/badge/aws-232F3E?style=for-the-badge&logo=amazonwebservices&logoColor=white">
  <img src="https://img.shields.io/badge/ec2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white">
  <br/>
</div>


## 인프라 아키텍처 설계 및 구성

![인프라 구성 excalidraw](https://github.com/user-attachments/assets/06d872ef-eabe-4747-87a8-063ff0fa0e88)
**구성 의도**
- 티켓팅 서버와 대기열 서버를 분리하고 ALB를 이용한 부하 분산
- Public subnet과 Private subnet을 분리하여 보안 강화

**문제점**
- 모든 서버가 동일한 가용영역에 배치되어 고가용성이 보장되지 않음
- ALB를 Public subnet에 배치했기 때문에 스프링 서버를 Public subnet에 배치할 이유가 없음
- 위의 이유들로 ALB의 효과를 보지 못함

프로젝트는 종료되었지만 위 문제점을 인식하여 다음과 같은 개선안을 그려보았습니다.

### 개선안

<img width="1125" alt="image" src="https://github.com/user-attachments/assets/eb868c99-6b0e-46a1-b784-93626937c834" />


**설명**
- 각 서버를 서로 다른 가용 영역에 배치하고 추가적인 Scale Out으로 가용성을 높였습니다.
- ALB와 NAT만 Public Subnet에 배치하고, 모든 서비스를 Private subnet에 배치하였습니다.
- Private Subnet에 접근할 수 있는 Bastion Host를 배치하였습니다.
- Redis Sentinel과 RDS Multi-AZ 구성을 통해 단일 장애 지점을 방지합니다.

## 티켓팅
### 동시성 문제 발생 시나리오
![image](https://github.com/user-attachments/assets/95d704fe-ac3c-4e69-acc7-80cb4b09c694)
티켓팅은 위와 같이 좌석에 대한 경합이 일어나는 상황에 초점을 맞춰 구현하였습니다.
위 그림은 아래 플로우를 표현합니다.
1. 갑과 을이 좌석 목록을 조회하고 A 좌석이 비어있음을 확인합니다.
2. 갑이 A좌석 예매를 위한 트랜잭션을 시작하고 A 좌석이 예매 가능한 상태인 것을 확인합니다. 
3. 을 또한 A좌석 예매를 위한 트랜잭션을 시작하고 A 좌석이 예매 가능한 상태인 것을 확인합니다.
4. 갑은 성공적으로 A 좌석 예매를 성공합니다.
5. 을 또한 A 좌석 예매에 성공하며 더블 부킹(데이터 부정합) 문제가 발생합니다.

따라서 그림의 1번과 2번 지점에 락을 사용하여 동시성을 제어합니다.  

### 동시성 제어를 위한 다양한 Lock 방식 구현 및 적용
락은 다음과 같은 방법들을 구현하고 비교해보았습니다.
- 비관락 (MySQL 배타락) 
- Version 정보를 이용한 낙관락 (JPA)
- Redis Pub/Sub락
- Redis Spin락

각 방식을 구현하기 위해 인터페이스로 추상화하고, 각각의 기능 구성에 필요한 구현체를 Configuration 클래스에서 일괄적으로 빈으로 등록할 수 있도록 하였습니다.  
또한, 동시성 테스트를 통해 기능에 대한 검증까지 마쳤습니다. 

### 부하 테스트 및 성능 비교
구현한 티켓팅 기능에 대하여 부하테스트 및 성능 비교를 진행하였습니다.
부하 테스트는 아래와 같은 환경에서 진행했습니다.
- 테스트 도구: Locust (Distribute Mode)
  - 구성: Master 1개(EC2 t3.small), Slave 10개(EC2 t3.small) 사용
  - 가상 유저 2500 명
- 서버 구성
  - 티켓팅 서버: EC2 t3.small
  - Redis 서버: EC2 t3.small
  - 테스트 데이터베이스: RDS t3.micro
  - 테스트 데이터 구성: 좌석 갯수 500개 (다양한 개수로 실험해 보았으나 너무 많은 좌석은 경합을 줄인다고 판단 되었음)

최초에는 개인 PC에서 Locust를 실행하여 부하 테스트를 해보려 했으나, 단일 PC 리소스 제약으로 인해 Locust와 스크립트를 담은 커스텀 AMI를 만들고 여러대 복제하여 큰 부하를 줄 수 있는 환경을 구성했습니다.

**테스트 시나리오**  
1. 사용자가 좌석 목록을 조회합니다.
2. 좌석 목록 중 하나를 랜덤으로 선택하여 예매를 시도합니다.

**테스트 결과**

<img width="827" alt="image" src="https://github.com/user-attachments/assets/77d97765-27a2-4052-9adb-397ca9111823" />

테스트를 수행하기 전, 레디스를 활용한 pub/sub락의 성능이 가장 좋을 것이라고 예상했습니다.  
하지만 결과는 정 반대로 나타났습니다.
```
DB 비관락 >= 논리적 낙관락(JPA) >= Redis Spin락 > Redis Pub/Sub 락
```
이러한 결과를 분석하여 다음과 같은 인사이트를 얻었습니다.
- 단일 DB 시스템에서는 Redis를 활용한 분산락을 적용하는 것이 적절하지 않은 것으로 보입니다. 만약 분산 DB 및 서버 환경이라면 개별적인 DB락으로는 전체 흐름에 대한 동시성 제어가 어렵기 때문에 이런 상황에서 분산락을 사용한다면 더 효과적일 수 있을 것 입니다.
- MySQL은 MVCC를 사용하기 때문에 비관락을 사용하더라도 단순 조회에 제약을 걸지 않습니다. 그렇기 때문에 재시도를 하지 않는 낙관락과 비교해도 성능이 밀리지 않습니다. 다만 작업이 여러 번의 트랜잭션에 걸쳐서 수행된다면 비관락만으로는 동시성 제어가 어려울 수 있기 때문에 이러한 상황에서는 낙관락이 유용할 수 있습니다.
- 좌석은 한 번 선점되면 이 후로는 경쟁이 일어나지 않습니다. 이렇게 경합이 크게 일어나지 않는 상황에서는 Spin락에서의 Spin이 거의 일어나지 않으므로 이벤트 발행 및 구독으로 인한 Pub/Sub 락의 비용이 더 큰 것으로 추정됩니다.   
Pub/Sub 락을 효과적으로 활용할 수 있는 상황은 **선착순 쿠폰 이벤트**처럼 동일한 자원에 대한 경쟁이 지속적으로 일어나는 상황이라고 추측할 수 있었습니다. 


## 대기열
### 세션 유지가 가능한 대기열 구현
티켓팅을 하다보면 사소한 실수로 오랫동안 기다린 대기열에서 이탈되는 경우가 있습니다. 이런 경험을 개선하기 위해 세션이 유지되는 대기열 기능을 구현하였습니다.

![image](https://github.com/user-attachments/assets/51822f9c-8eea-44c3-b84b-373179a0f861)

대기열 시스템은 위와 같이 은행 창구의 대기번호표 시스템을 모티브로 구현하여 다음과 같이 동작하도록 설계하였습니다.

1. 대기 번호 발급기는 새로 온 사용자에게 대기 번호를 발급합니다. 
2. 입장 번호 표시기는 작업 공간의 여유를 확인하고 입장할 수 있는 가장 큰 번호를 호출합니다. 
3. 2번에서 호출한 번호보다 작은 번호표를 가진 사용자들은 대기 공간에서 나가고, 작업 공간의 세션을 얻습니다. 
4. 티켓팅을 완료하거나 시간이 만료된 사용자들의 세션을 삭제합니다.
5. 위 작업이 반복됩니다.

대기번호표 시스템 방식으로 구현하였기 때문에 단순한 수식으로 대기번호를 쉽게 계산할 수 있습니다.
```
입장번호 카운트 - 대기번호 카운트
```

이에대한 실제 구현은 Redis를 사용했습니다.
번호표 카운트와 대기번호 카운트는 INCR을 활용해 원자적인 연산으로 동시성이 발생하지 않도록 했습니다.
대기열 큐는 Sorted List를, 티켓팅 세션은 HashSet으로 구현하였습니다.


### 대기열 제어 전체 흐름

![image](https://github.com/user-attachments/assets/a0bd5cf8-e86c-497b-ac00-5610246fa42e)

위 그림은 다음과 같은 시나리오를 설명합니다.

1. 좌석 조회 API를 호출하면 AOP를 통해 사용자가 좌석 예매를 위한 세션이 있는지 확인합니다.
2. 세션이 없으면 리다이렉트되어 대기열 페이지로 이동합니다.
3. 사용자는 1초 주기로 서버에 폴링하여 대기열이 얼마나 남았는지 확인합니다.
4. Debounce를 이용해 5초에 한 번만 대기열 이동 작업을 수행하도록 하여 서버 부하를 조절하였습니다.
    - 내부적으로는 대기열 큐에서 사용자를 제거하고 티켓팅 세션을 생성해야하는데, 사용자의 폴링이 일어날때마다 이러한 읽기 쓰기 작업이 일어나는 것을 조절하기 위함입니다.

### 부하 테스트 및 성능 개선
> 평균 응답 시간 약 210 ms -> 41ms 으로 **5배 이상 개선**

대기열 역시 Locust를 사용해 부하 테스트를 진행했습니다.
부하테스트 환경은 t3.small, 가상유저 2500명으로 티켓팅 기능과 비슷한 환경에서 테스트하였습니다.

![image](https://github.com/user-attachments/assets/a4a39c67-213b-4a94-87bc-df6eeed93971)


위 사진은 응답 시간과 Heap Memory 메트릭을 시각화 한 것입니다. Heap 메모리에 변동이 있는 순간과 응답시간 지연이 발생하는 부분이 많은 구간에서 일치하는 것을 발견했습니다.

해당 수치를 보고 GC가 일어나면서 Stop The World로 인해 응답시간 지연이 발생하고 있다고 추측했습니다.

따라서, GC의 발생 빈도를 줄이고자 다음과 같은 작업을 하였습니다.
- 반복적으로 사용되는 Application Event 객체 재활용 (요청마다 생성 -> 하나의 공연당 하나의 이벤트만 생성)
- Redis에 저장되는 사용자 세션 DTO 객체의 필드를 최소화하여 객체 비용 및 직렬화 비용 최적화

![image](https://github.com/user-attachments/assets/db591d18-a2b6-49c7-a216-e8666d046175)

최적화 작업 이후 위 사진과 같이 응답시간 스파이크가 많이 줄어드는 것을 확인하였습니다. 또한 평균 응답 시간도 약 210ms -> 100ms 으로 2배 이상 향상되었습니다.



하지만 여전히 의문이 남는 부분이 있었습니다. 개선 전후 공통적으로 Old 영역의 메모리가 점진적으로 증가하다가 줄어든다는 점 입니다.
대기열 서버는 Bean을 제외하고 메모리에 캐싱하거나 오랜 주기로 사용되는 객체가 없기 때문에 Old 영역에 객체가 쌓일 일이 없다고 생각했고 추가적인 분석을 하게 되었습니다.


![image](https://github.com/user-attachments/assets/9e19423f-0fbd-4f3f-ad19-840db67ca698)

위 사진은 MAT를 통해 얻은 Heap Dump와 GC log을 확인한 결과입니다. 위 결과들을 모두 종합해보고 다음과 같은 사실들을 알게되었습니다.

- Old영역에 객체가 쌓이는 이유는 너무 많은 요청으로 인해 Young(Suvivor) 영역에서 오래되지 않은 객체들이 강제로 Old 영역으로 이동했기 때문이다.
  - 실제로 Old 영역이 증가하는 폭을 보면 자동 할당되는 Suvivor의 영역 크기와 비슷하다. 
  ```
  JVM Max Heap Memory = T3.small Memory(2G) * 1/4 = 약 500MB
  Young Area = 500MB * 1/3 ( default Old:Young = 2:1 ) = 약 170MB
  Suvivor Area = 170MB * 2/10 (default Eden:Suv = 8:2) = 약 35MB
  ```
- Old영역이 초기에 Young 영역보다 많이 할당되지만 GC가 이를 최적화하여 Young 영역의 크기가 늘어난다.

실제로 GC로그를 심층분석해본 결과 초기에는 **37개의 Region**이던 Young 영역이 애플리케이션 실행 도중 **175개**로 늘어난 것을 확인했습니다.

분석 결과를 바탕으로 Young 영역을 처음부터 크게 할당하여 최종 부하테스트를 수행했습니다. 

#### 실행 Command
```
java -jar -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=60 -Xlog:gc*=info:file=gc.log:time,uptime,level,tags -Dspring.profiles.active=prod ticketing-0.0.1-SNAPSHOT.jar
```

#### 테스트 결과
![image](https://github.com/user-attachments/assets/6a5f4ff6-c5f8-4721-9759-7fe9ab36f570)



- **응답 시간 (상위 50%) : 210ms -> 41ms**
- **응답 시간 (상위 95%) : 660ms -> 150ms**

전체적으로 응답속도가 4배 이상 향상했음을 볼 수 있습니다.
이를 통해 Scale-up, Out 없이 성능을 4배 개선하는 성과를 낼 수 있었습니다.

### 번외 - 좌석 예매 여부 실시간 알림 (SSE)
<img src="https://github.com/user-attachments/assets/87a1ed80-2a65-4836-a0be-f75dc9fccdcf" width="444" height="507"/>  

"이미 예약된 좌석입니다" 문제로 인해 티켓팅을 실패하는 부정적 경험을 줄이기 위해 좌석 예매 여부를 실시간으로 알리는 기능을 SSE로 구현해보았습니다.

하지만 SSE 기능을 포함하면 서비스가 불가능할 정도로 부하가 심했기 때문에 해당 기능은 실제로 적용하기엔 어렵다고 판단했습니다.

## 기타 - 애자일하게 협업하기

<img width="361" alt="image" src="https://github.com/user-attachments/assets/19cea060-11da-4303-a04e-8a50786ddc2a" />

Github Projects, Issue를 활용해 해야하는 작업을 백로그로 정의하며 협업하였습니다.
요구사항은 가능한 작은 단위로 분리하여 우선순위을 정하고 시간을 추정하여 백로그로 정의했습니다.
이슈는 PR merge 이후 close 되도록 하였으며 각 PR은 최소 한 명 이상의 Code Review 및 Approve가 있어야 merge할 수 있도록 하였습니다.

작업을 작은 단위로 분리하고, 서로의 작업을 리뷰하면서 직접 구현한 기능이 아니더라도 서로가 구현하는 기능을 이해할 수 있었으며 작업 진행 상황이 잘 공유된다는 장점이 있었습니다.


### 전체 시나리오 시연

https://github.com/user-attachments/assets/eb29c948-4a1c-41fd-a7b5-b6f4d167969c

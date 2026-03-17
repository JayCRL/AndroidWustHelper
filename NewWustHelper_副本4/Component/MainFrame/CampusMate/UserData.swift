//
//  UserData.swift
//  NewWustHelper
//
//  Created by wust_lh on 2025/8/5.
//

import SwiftUI

class UserData: ObservableObject {
    //教务系统信息
    @AppStorage("studentInfo") private var studentInfoJson: String = ""
    var studentInfo: StudentInfo {
        get {
            guard let data = studentInfoJson.data(using: .utf8),
                  let decoded = try? JSONDecoder().decode(StudentInfo.self, from: data)
            else {
                return StudentInfo()  // 默认值
            }
            return decoded
        }
        set {
            if let data = try? JSONEncoder().encode(newValue),
               let jsonString = String(data: data, encoding: .utf8) {
                studentInfoJson = jsonString
            }
        }
    }
    @Published var isLoading: Bool = false  // 使用 StateObject 管理加载数据的状态
    //搭子平台用户信息
    @Published var userInfo: UserInfo?
    //活动内容
    @Published var activities: [Activity] = []
    //通知
    @Published var Noti: [Noti] = []
    //草稿
    @Published var drafts: [Draft] = []
    //活动评论
    @Published var activityComments: [Int: [Comment]] = [:]
    //活动状态
    @Published var activityStats: [Int: ActivityStats] = [:]
    //用户核验信息
    @AppStorage("authData") var authToken: String = ""
    //个人点赞过的id
    @Published var likedActivityIDs: Set<Int> = []
    //个人收藏过的id
    @Published var FavoriteActivityIDs: Set<Int> = []
    //个人申请过的id
    // 定义字典类型：key为Int(activityId)，value为String(status)
    @Published var applicationStatusMap: [Int: String] = [:]
    @Published var applicationIdMap: [Int: Int] = [:]
    //选中的活动id
    @State   var activityID:Int=0
    // 获取当前用户信息
    // 先定义一个网络错误枚举
    enum NetworkError: Error {
        case invalidURL
        // 可以根据需要添加其他错误类型，如请求失败、解析错误等
    }
    func fetchUserInfo() {
        guard !authToken.isEmpty else { return }
        guard let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/user/me")else{
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else { return }
            do {
                if let jsonString = String(data: data, encoding: .utf8) {
                    print("返回的原始JSON字符串：\n\(jsonString)")
                } else {
                    print("无法将返回的数据转为 UTF-8 字符串")
                }
                let response = try JSONDecoder().decode(ApiResponse<UserInfo>.self, from: data)
                if response.code == 0, let userInfo = response.data {
                    DispatchQueue.main.async {
                        self.userInfo = userInfo
                    }
                }
                
            } catch {
                print("Error decoding user info: \(error)")
            }
        }.resume()
    }
    func fetchUserNotification(userInfo: UserInfo, completion: @escaping (Bool) -> Void) {
        guard !authToken.isEmpty else { return }
        let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/notifications")!
        var request = URLRequest(url: url)
        request.httpMethod = "Get"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        do {
            request.httpBody = try JSONEncoder().encode(userInfo)
        } catch {
            print("Error encoding user info: \(error)")
            completion(false)
            return
        }
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                completion(false)
                return
            }
            
            do {
                let response = try JSONDecoder().decode(ApiResponse<[Noti]>.self, from: data)
                if response.code == 0, let notis = response.data {
                    DispatchQueue.main.async {
                        self.Noti = notis
                        print("成功获取通知！")
                    }
                }
        
            } catch {
                print("Error decoding update response: \(error)")
                completion(false)
            }
        }.resume()
    }
        // 更新用户信息
    func updateUserInfo(userInfo: UserInfo, completion: @escaping (Bool) -> Void) {
        guard !authToken.isEmpty else { return }
        let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/user/me")!
        var request = URLRequest(url: url)
        request.httpMethod = "PUT"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        do {
            request.httpBody = try JSONEncoder().encode(userInfo)
        } catch {
            print("Error encoding user info: \(error)")
            completion(false)
            return
        }
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                completion(false)
                return
            }
            do {
                let response = try JSONDecoder().decode(ApiResponse<UserInfo>.self, from: data)
                DispatchQueue.main.async {
                    self.userInfo = userInfo
                    print(userInfo)
                        completion(true)
                    
                }
            } catch {
                print("Error decoding update response: \(error)")
                completion(false)
            }
        }.resume()
    }
    // MARK: - 核心2：新增查询图片详情方法
    func getProfilePictureId(userId: Int, completion: @escaping (Int) -> Void) {
        guard !authToken.isEmpty else { return }
        let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/user/profileurl/\(userId)")!
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                completion(0)
                return
            }
            do {
                let response = try JSONDecoder().decode(ApiResponse<Int>.self, from: data)
                if response.code == 0, let notis = response.data {
                    DispatchQueue.main.async {
                        completion(notis)
                        print("成功获取头像！")
                    }
                }
            } catch {
                print("Error decoding update response: \(error)")
                completion(0)
            }
        }.resume()
    }
    // 获取活动列表
    func fetchActivities(campus: String? = nil, college: String? = nil, type: String? = nil) {
        guard !authToken.isEmpty else { return }
        guard var components = URLComponents(string: "\(BasicValue.CampusMatebaseUrl)/api/activities")else {
            return
        }
        var queryItems: [URLQueryItem] = []
        if let campus = campus { queryItems.append(URLQueryItem(name: "campus", value: campus)) }
        if let college = college { queryItems.append(URLQueryItem(name: "college", value: college)) }
        if let type = type { queryItems.append(URLQueryItem(name: "type", value: type)) }
        components.queryItems = queryItems
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
               guard let data = data else { return }
//               if let rawString = String(data: data, encoding: .utf8) {
//                   print("🔥 Raw Activity : \(rawString)")
//               } else {
//                  print("⚠️ Response  Activity")
//                  }
               do {
                   let response = try JSONDecoder().decode(ApiResponse<[Activity]>.self, from: data)
                   if response.code == 0, let activities = response.data {
                       DispatchQueue.main.async {
                           self.activities = activities
                           // ✅ 加载评论
                           for activity in activities {
                               self.fetchComments(for: activity.id)
                           }
                           //获取点赞id
                           self.fetchLikedId()
                           self.fetchFavoriteId()
                           self.fetchAllActivityStats()
                       }
                   }
               } catch {
                   print("Error decoding act    ivities: \(error)")
               }
           }.resume()
    }
    func fetchActivity(RelatedId: Int, completion: @escaping (Activity) -> Void) {
        guard !authToken.isEmpty else { return }
        let components = URLComponents(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/noti/\(RelatedId)")!
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
               guard let data = data else { return }
               do {
                   let response = try JSONDecoder().decode(ApiResponse<Activity>.self, from: data)
                   if response.code == 0, let activities = response.data {
                       DispatchQueue.main.async {
                           completion(response.data!)
                       }
                   }
               } catch {
                   print("Error decoding activities: \(error)")
               }
           }.resume()
    }
    // 获取活动评论列表
    func fetchComments(
        for activityId: Int,
        page: String? = nil,
        size: String? = nil
    ) {
        guard !authToken.isEmpty else { return }
        var components = URLComponents(string: "\(BasicValue.CampusMatebaseUrl)/api/comments/activity/getAllComments/\(activityId)")!
        var queryItems: [URLQueryItem] = []
        if let page = page { queryItems.append(URLQueryItem(name: "page", value: page)) }
        if let size = size { queryItems.append(URLQueryItem(name: "size", value: size)) }
        components.queryItems = queryItems
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")

        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                print("No data: \(error?.localizedDescription ?? "Unknown error")")
                return
            }
            do {
                let decoded = try JSONDecoder().decode(ApiResponse<[Comment]>.self, from: data)
                if decoded.code == 0, let comments = decoded.data {
                    DispatchQueue.main.async {
                        // 将获取到的评论写入 activityComments 字典
                        self.activityComments[activityId] = comments
                    }
                } else {
                    print("Server error: \(decoded.msg)")
                }
            } catch {
                print("Decoding error: \(error)")
            }
        }.resume()
    }
    func fetchLikedId() {
        guard !authToken.isEmpty else { return }
        guard  var components = URLComponents(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/Activity/getAllLikedId")else{
            return
        }
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                print("No data: \(error?.localizedDescription ?? "Unknown error")")
                return
            }
            do {
                let decoded = try JSONDecoder().decode(ApiResponse<[LikeRecord]>.self, from: data)
                if decoded.code == 0, let likeRecords = decoded.data {
                    DispatchQueue.main.async {
                        self.likedActivityIDs = Set(likeRecords.map { $0.activityId })
                    }
                } else {
                    print("Server error: \(decoded.msg)")
                }
            } catch {
                print("Decoding error: \(error)")
            }
        }.resume()
    }
    func fetchFavoriteId() {
        guard !authToken.isEmpty else { return }
        guard let components = URLComponents(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/user/favoritesId")else{
            return
        }
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                print("No data: \(error?.localizedDescription ?? "Unknown error")")
                return
            }
//            if let rawString = String(data: data, encoding: .utf8) {
//                print("🔥 Raw response: \(rawString)")
//            } else {
//                print("⚠️ Response is nil or not UTF-8")
//            }
            do {
                let decoded = try JSONDecoder().decode(ApiResponse<[Favorite]>.self, from: data)
                if decoded.code == 0, let FavoritesRecords = decoded.data {
                    DispatchQueue.main.async {
                        self.FavoriteActivityIDs = Set(FavoritesRecords.map { $0.activityId })
                    }
                } else {
                    print("Server error: \(decoded.msg)")
                }
            } catch {
                print("Decoding error: \(error)")
            }
        }.resume()
    }
    func fetchApply() {
        guard !authToken.isEmpty else { return }
        guard var components = URLComponents(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/user/ApplicationsDTO")else {
            return
        }
        var request = URLRequest(url: components.url!)
        request.httpMethod = "GET"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                print("No data: \(error?.localizedDescription ?? "Unknown error")")
                return
            }
            do {
                if let rawString = String(data: data, encoding: .utf8) {
                    print("🔥 Raw Activity : \(rawString)")
                } else {
                   print("⚠️ Response  Activity")
                   }
                // 解码为包含ApplyRecord数组的响应
                let decoded = try JSONDecoder().decode(ApiResponse<[ApplyRecord]>.self, from: data)
                if decoded.code == 0, let applyRecords = decoded.data {
                    DispatchQueue.main.async {
                        // 转换为activityId: status的字典
                        self.applicationStatusMap = applyRecords.reduce(into: [:]) { $0[$1.activityId] = $1.status }
                        self.applicationIdMap = applyRecords.reduce(into: [:]) { $0[$1.activityId] = $1.id }
                    }
                } else {
                    print("服务器错误: \(decoded.msg)")
                }
            } catch {
                print("解码错误: \(error)")
            }
        }.resume()
    }
    //删除活动
    func deleteActivity(activityId: Int, completion: @escaping (Bool) -> Void) {
        guard !authToken.isEmpty else {
            completion(false)
            return
        }
        
        let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/\(activityId)")!
        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                completion(false)
                return
            }
            
            do {
                let response = try JSONDecoder().decode(ApiResponse<Bool>.self, from: data)
                DispatchQueue.main.async {
                    completion(response.code == 0 && response.data == true)
                }
            } catch {
                print("Error decoding delete response: \(error)")
                completion(false)
            }
        }.resume()
    }
    // 点赞活动
    func likeActivity(activityId: Int, completion: @escaping (Bool) -> Void) {
        guard !authToken.isEmpty else {
            completion(false)
            return
        }
        let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/\(activityId)/like")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                completion(false)
                return
            }
            
            do {
                if let jsonString = String(data: data, encoding: .utf8) {
                    print("Response JSON: \(jsonString)")
                }
                let response = try JSONDecoder().decode(ApiResponse<Bool>.self, from: data)
                DispatchQueue.main.async {
                   
                    completion(response.code == 0)
                    
                }
            } catch {
                print("Error decoding like response: \(error)")
                completion(false)
            }
        }.resume()
    }
    // 收藏活动
    func FavoriteActivity(activityId: Int, completion: @escaping (Bool) -> Void) {
        guard !authToken.isEmpty else {
            completion(false)
            return
        }
        let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/activities/\(activityId)/favorite")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                completion(false)
                return
            }
            
            do {
                if let jsonString = String(data: data, encoding: .utf8) {
                    print("Response JSON: \(jsonString)")
                }
                let response = try JSONDecoder().decode(ApiResponse<Bool>.self, from: data)
                DispatchQueue.main.async {
                   
                    completion(response.code == 0)
                    
                }
            } catch {
                print("Error decoding like response: \(error)")
                completion(false)
            }
        }.resume()
    }
    // 申请参加活动
    func applyForActivity(activityId: Int, reason: String, completion: @escaping (Bool) -> Void) {
        guard !authToken.isEmpty else { return }
        
        // 构建请求URL
        let urlString = "\(BasicValue.CampusMatebaseUrl)/api/activities/\(activityId)/apply?reason=\(reason.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? "")"
        guard let url = URL(string: urlString) else {
            completion(false)
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                completion(false)
                return
            }
            
            do {
                let response = try JSONDecoder().decode(ApiResponse<Bool>.self, from: data)
                DispatchQueue.main.async {
                    if response.code == 0 {
                        completion(true)
                    } else {
                        completion(false)
                    }
                }
            } catch {
                print("Error decoding application response: \(error)")
                completion(false)
            }
        }.resume()
    }
    // 取消申请活动
    func CancleApplyForActivity(appid: Int, completion: @escaping (Bool) -> Void) {
        guard !authToken.isEmpty else { return }
        let urlString = "\(BasicValue.CampusMatebaseUrl)/api/activities/applications/\(appid)/cancle"
        guard let url = URL(string: urlString) else {
            print("❌ 无效的 URL")
            return
        }
        var request = URLRequest(url: url)
        request.httpMethod = "PUT"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                completion(false)
                return
            }
            
            do {
                if let jsonString = String(data: data, encoding: .utf8) {
                    print("Response JSON: \(jsonString)")
                }
                let response = try JSONDecoder().decode(ApiResponse<Bool>.self, from: data)
                DispatchQueue.main.async {
                    if response.code == 0 {
                        completion(true)
                    } else {
                        completion(false)
                    }
                }
            } catch {
                print("Error decoding application response: \(error)")
                completion(false)
            }
        }.resume()
    }
    //获取状态
    func fetchActivityStats(activityId: Int) {
        guard !authToken.isEmpty else { return }
        let urlString = "\(BasicValue.CampusMatebaseUrl)/api/activities/\(activityId)/stats"
        guard let url = URL(string: urlString) else {
            print("❌ 无效的 URL")
            return
        }
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        print("ok")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                print("❌ 网络请求失败: \(error?.localizedDescription ?? "未知错误")")
                return
            }
            if let jsonString = String(data: data, encoding: .utf8) {
                print("Response JSON: \(jsonString)")
            }
            do {
                let response = try JSONDecoder().decode(ApiResponse<ActivityStats>.self, from: data)
                if response.code == 0, let stats = response.data {
                    DispatchQueue.main.async {
                        print(stats)
                        self.activityStats[activityId]=stats // 假设你有个变量来存
                    }
                } else {
                    print("⚠️ 接口返回错误: \(response.msg)")
                }
            } catch {
                print("❌ JSON 解码失败: \(error)")
            }
        }.resume()
    }
    func fetchAllActivityStats() {
        for activity in activities {
            fetchActivityStats(activityId: activity.id)
        }
    }
    func signRead(notifyId:Int) {
        guard !authToken.isEmpty else { return }
        let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/notifications/\(notifyId)/read")!
        var request = URLRequest(url: url)
        request.httpMethod = "PUT"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else { return }
            do {
                let response = try JSONDecoder().decode(ApiResponse<Bool>.self, from: data)
                if let jsonString = String(data: data, encoding: .utf8) {
                    print("Response JSON: \(jsonString)")
                }
                if response.code == 0, let result = response.data {
                    DispatchQueue.main.async {
                        print("标为已读成功")
                    }
                }
            } catch {
                print("Error decoding notifications: \(error)")
            }
        }.resume()
    }
    // 获取通知列表
    func fetchNotifications() {
        print("开始获取通知👍")
        guard !authToken.isEmpty else { return }
        guard    let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/notifications") else{
            return
        }
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else { return }
            do {
                let response = try JSONDecoder().decode(ApiResponse<[Noti]>.self, from: data)
                if let jsonString = String(data: data, encoding: .utf8) {
                    print("Response JSON: \(jsonString)")
                }
                if response.code == 0, let notifications = response.data {
                    DispatchQueue.main.async {
                        self.Noti = notifications
                    }
                }
            } catch {
                print("Error decoding notifications: \(error)")
            }
        }.resume()
    }
    
    // 创建活动
    func createActivity(activity: Activity, completion: @escaping (Bool) -> Void) {
        guard !authToken.isEmpty else { return }
        
        let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/activities")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        
        do {
            request.httpBody = try JSONEncoder().encode(activity)
        } catch {
            print("Error encoding activity: \(error)")
            completion(false)
            return
        }
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else {
                completion(false)
                return
            }
            
            do {
                let response = try JSONDecoder().decode(ApiResponse<Activity>.self, from: data)
                DispatchQueue.main.async {
                    if response.code == 0 {
                        self.fetchActivities()
                        completion(true)
                    } else {
                        completion(false)
                    }
                }
            } catch {
                print("Error decoding activity response: \(error)")
                completion(false)
            }
        }.resume()
    }
    // MARK: - Models

    struct ApiResponse<T: Decodable>: Decodable {
        let code: Int
        let msg: String
        let data: T?
    }

    struct PageResponse<T: Decodable>: Decodable {
        let totalPages: Int
        let totalElements: Int
        let size: Int
        let content: [T]
        let number: Int
        let sort: Sort
        let first: Bool
        let last: Bool
        let numberOfElements: Int
        let pageable: Pageable
        let empty: Bool
    }

    struct Sort: Decodable {
        let empty: Bool
        let sorted: Bool
        let unsorted: Bool
    }

    struct Pageable: Decodable {
        let offset: Int
        let sort: Sort
        let unpaged: Bool
        let paged: Bool
        let pageNumber: Int
        let pageSize: Int
    }
    // 定义评论数据模型
    struct Comment: Codable, Identifiable {
        // 根据服务器返回的评论结构定义相应的属性
        let id: Int
        let activityId: Int
        let userId: Int
        let content: String
        let parentId: Int?
        let createdAt: String
    }
    struct likedId: Codable {
        // 根据服务器返回的评论结构定义相应的属性
        let ActivityId:Bool
    }
    struct NetworkComment: Codable {
        let id: Int
        let activityId: Int
        let userId: Int
        let content: String
        let parentId: Int?
        let createdAt: String
    }
    // 定义服务器响应模型
    struct CommentResponse: Codable {
        let code: Int
        let message: String?
        let data: Comment?
    }
    // 添加评论
    func DeleteComments(commentID: Int, completion: @escaping (Bool) -> Void) {
        guard !authToken.isEmpty else { return }
        let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/comments/\(commentID)")!
        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")
        URLSession.shared.dataTask(with: request) { data, response, error in
            guard let data = data else { return }
            do {
                let response = try JSONDecoder().decode(ApiResponse<Bool>.self, from: data)
                if let jsonString = String(data: data, encoding: .utf8) {
                    print("Response JSON: \(jsonString)")
                }
                if response.code == 0, let result = response.data {
                    DispatchQueue.main.async {
                        print("删除成功")
                        completion(true)
                    }
                }
            } catch {
                print("Error decoding notifications: \(error)")
            }
        }.resume()
    }
    // 添加评论
    func addComment(activityId: Int, content: String, parentId: Int? = nil, completion: @escaping (Bool) -> Void) {
        // 验证用户状态
        guard !authToken.isEmpty, let userInfo = userInfo else {
            completion(false)
            return
        }

        // 请求URL
        let url = URL(string: "\(BasicValue.CampusMatebaseUrl)/api/comments")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("Wuster \(authToken)", forHTTPHeaderField: "Authorization")

        let dateFormatter = ISO8601DateFormatter()
        dateFormatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        let currentDateString = dateFormatter.string(from: Date())
        // 请求体
        let requestBody: [String: Any] = [
            "activityId": activityId,
             "userId": 0,
            "content": content,
            "parentId": parentId,
             "createdAt": "2025-08-01T03:48:39.878Z"
        ]
        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: requestBody)
        } catch {
            print("JSON Encoding Error: \(error)")
            completion(false)
            return
        }

        // 发起请求
        URLSession.shared.dataTask(with: request) { data, response, error in
            // 网络错误处理
            if let error = error {
                print("Network Error: \(error)")
                completion(false)
                return
            }

            guard let data = data else {
                print("No data received")
                completion(false)
                return
            }

            // 打印原始 JSON 响应
            if let jsonString = String(data: data, encoding: .utf8) {
                print("Response JSON: \(jsonString)")
            }

            do {
                // 解码服务端响应
                let response = try JSONDecoder().decode(ApiResponse<NetworkComment>.self, from: data)
                let workItem = DispatchWorkItem {
                    print("执行任务")
                }
                //更新本地数据
                DispatchQueue.main.async(execute: {
                    if response.code == 0, let newComment = response.data {
                        let viewComment = Comment(
                            id: newComment.id,
                            activityId: newComment.activityId, userId: userInfo.userId,
                            content: newComment.content,
                            parentId: newComment.parentId,
                            createdAt: newComment.createdAt
                        )
                        // 更新本地评论数据
                        if var activityComments = self.activityComments[activityId] {
                            activityComments.append(viewComment)
                            self.activityComments[activityId] = activityComments
                        } else {
                            self.activityComments[activityId] = [viewComment]
                        }
                        completion(true)
                    }else {
                        print("API Error \(response.code): \(response.msg)")
                        completion(false)
                    }
                })


            } catch {
                print("Decoding Error: \(error)")
                completion(false)
            }
        }.resume()
    }
}

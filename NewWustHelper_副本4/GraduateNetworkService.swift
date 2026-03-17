//
//  GraduateNetworkService.swift
//  NewWustHelper
//
//  Created for graduate student system integration
//

import Foundation

// MARK: - 研究生网络错误
enum GraduateNetworkError: Error, LocalizedError {
    case invalidURL
    case serializationFailed
    case networkError(String)
    case noData
    case serverError(String)
    case parsingFailed(String)
    
    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "接口地址无效"
        case .serializationFailed:
            return "数据序列化失败"
        case .networkError(let message):
            return "网络请求失败: \(message)"
        case .noData:
            return "服务器无返回数据"
        case .serverError(let message):
            return message
        case .parsingFailed(let message):
            return "数据解析失败: \(message)"
        }
    }
}

// MARK: - 研究生课程模型
struct GraduateCourse: Codable {
    let name: String
    let teacher: String
    let teachClass: String
    let startWeek: Int
    let endWeek: Int
    let weekDay: Int
    let startSection: Int
    let endSection: Int
    let classroom: String
}

// MARK: - 研究生登录凭据
struct GraduateLoginCredentials {
    let studentId: String
    let password: String
    
    func toJSON() -> [String: String] {
        return [
            "student_id": studentId,
            "password": password
        ]
    }
}

// MARK: - 研究生网络服务
class GraduateNetworkService {
    
    /// 获取课程表
    /// - Parameters:
    ///   - credentials: 登录凭据
    ///   - completion: 完成回调，返回课程数组或错误信息
    static func fetchCourseSchedule(
        credentials: GraduateLoginCredentials,
        completion: @escaping (Result<[GraduateCourse], GraduateNetworkError>) -> Void
    ) {
        let urlString = "\(BasicValue.graduateCourseBaseUrl)/course"
        print("🔵 [研究生-课程] 开始获取课程表")
        print("🔵 [研究生-课程] URL: \(urlString)")
        print("🔵 [研究生-课程] 账号: \(credentials.studentId)")
        
        guard let url = URL(string: urlString) else {
            print("❌ [研究生-课程] URL无效: \(urlString)")
            completion(.failure(.invalidURL))
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.timeoutInterval = 15
        
        guard let jsonData = try? JSONSerialization.data(withJSONObject: credentials.toJSON()) else {
            print("❌ [研究生-课程] 数据序列化失败")
            completion(.failure(.serializationFailed))
            return
        }
        
        if let jsonString = String(data: jsonData, encoding: .utf8) {
            print("🔵 [研究生-课程] 请求参数: \(jsonString)")
        }
        
        request.httpBody = jsonData
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                if let error = error {
                    print("❌ [研究生-课程] 网络请求失败: \(error.localizedDescription)")
                    completion(.failure(.networkError(error.localizedDescription)))
                    return
                }
                
                // 打印HTTP状态码
                if let httpResponse = response as? HTTPURLResponse {
                    print("🔵 [研究生-课程] HTTP状态码: \(httpResponse.statusCode)")
                }
                
                guard let data = data else {
                    print("❌ [研究生-课程] 服务器无返回数据")
                    completion(.failure(.noData))
                    return
                }
                
                // 打印原始响应数据
                if let dataString = String(data: data, encoding: .utf8) {
                    print("🔵 [研究生-课程] 服务器返回数据: \(dataString)")
                }
                
                // 尝试解析错误响应
                if let errorResponse = try? JSONDecoder().decode([String: String].self, from: data),
                   let errorMessage = errorResponse["error"] {
                    print("❌ [研究生-课程] 服务器返回错误: \(errorMessage)")
                    completion(.failure(.serverError(errorMessage)))
                    return
                }
                
                // 解析课程数据 - 支持两种格式：直接数组或包装格式
                do {
                    // 首先尝试直接解析为数组格式（直接返回课程数组）
                    let courses = try JSONDecoder().decode([GraduateCourse].self, from: data)
                    print("✅ [研究生-课程] 课程数据解析成功，共 \(courses.count) 门课程")
                    completion(.success(courses))
                } catch {
                    // 如果直接解析失败，尝试解析为包装格式
                    do {
                        // 尝试解析为 {"data": [...]} 格式
                        struct CourseResponse: Codable {
                            let data: [GraduateCourse]
                        }
                        let response = try JSONDecoder().decode(CourseResponse.self, from: data)
                        print("✅ [研究生-课程] 课程数据解析成功（包装格式），共 \(response.data.count) 门课程")
                        completion(.success(response.data))
                    } catch {
                        // 如果还是失败，尝试解析为 {"courses": [...]} 格式
                        do {
                            struct CourseResponseAlt: Codable {
                                let courses: [GraduateCourse]
                            }
                            let response = try JSONDecoder().decode(CourseResponseAlt.self, from: data)
                            print("✅ [研究生-课程] 课程数据解析成功（courses格式），共 \(response.courses.count) 门课程")
                            completion(.success(response.courses))
                        } catch {
                            print("❌ [研究生-课程] 课程数据解析失败: \(error.localizedDescription)")
                            print("❌ [研究生-课程] 解析错误详情: \(error)")
                            if let dataString = String(data: data, encoding: .utf8) {
                                print("❌ [研究生-课程] 原始数据: \(dataString)")
                            }
                            completion(.failure(.parsingFailed(error.localizedDescription)))
                        }
                    }
                }
            }
        }.resume()
    }
    
    /// 获取培养方案
    /// - Parameters:
    ///   - credentials: 登录凭据
    ///   - completion: 完成回调，返回HTML内容或错误信息
    static func fetchCultivationPlan(
        credentials: GraduateLoginCredentials,
        completion: @escaping (Result<String, GraduateNetworkError>) -> Void
    ) {
        let urlString = "\(BasicValue.graduateCultivationPlanBaseUrl)/cultivation-plan"
        print("🔵 [研究生-培养方案] 开始获取培养方案")
        print("🔵 [研究生-培养方案] URL: \(urlString)")
        print("🔵 [研究生-培养方案] 账号: \(credentials.studentId)")
        
        guard let url = URL(string: urlString) else {
            print("❌ [研究生-培养方案] URL无效: \(urlString)")
            completion(.failure(.invalidURL))
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.timeoutInterval = 15
        
        guard let jsonData = try? JSONSerialization.data(withJSONObject: credentials.toJSON()) else {
            print("❌ [研究生-培养方案] 数据序列化失败")
            completion(.failure(.serializationFailed))
            return
        }
        
        if let jsonString = String(data: jsonData, encoding: .utf8) {
            print("🔵 [研究生-培养方案] 请求参数: \(jsonString)")
        }
        
        request.httpBody = jsonData
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                if let error = error {
                    print("❌ [研究生-培养方案] 网络请求失败: \(error.localizedDescription)")
                    completion(.failure(.networkError(error.localizedDescription)))
                    return
                }
                
                // 打印HTTP状态码
                if let httpResponse = response as? HTTPURLResponse {
                    print("🔵 [研究生-培养方案] HTTP状态码: \(httpResponse.statusCode)")
                }
                
                guard let data = data else {
                    print("❌ [研究生-培养方案] 服务器无返回数据")
                    completion(.failure(.noData))
                    return
                }
                
                // 打印原始响应数据
                if let dataString = String(data: data, encoding: .utf8) {
                    print("🔵 [研究生-培养方案] 服务器返回数据: \(dataString)")
                }
                
                // 优先尝试直接解析为HTML字符串（API直接返回HTML）
                if let htmlString = String(data: data, encoding: .utf8) {
                    let trimmedHtml = htmlString.trimmingCharacters(in: .whitespacesAndNewlines)
                    if !trimmedHtml.isEmpty {
                        // 检查是否是HTML格式（包含<html>标签）
                        if trimmedHtml.hasPrefix("<!DOCTYPE") || trimmedHtml.hasPrefix("<html") || trimmedHtml.contains("<html") {
                            print("✅ [研究生-培养方案] 培养方案获取成功（直接HTML格式），HTML长度: \(trimmedHtml.count) 字符")
                            completion(.success(trimmedHtml))
                            return
                        }
                    }
                }
                
                // 尝试解析错误响应（JSON格式）
                if let errorResponse = try? JSONDecoder().decode([String: String].self, from: data),
                   let errorMessage = errorResponse["error"] {
                    print("❌ [研究生-培养方案] 服务器返回错误: \(errorMessage)")
                    completion(.failure(.serverError(errorMessage)))
                    return
                }
                
                // 尝试解析为JSON格式（向后兼容，格式为 {"html": "..."}）
                do {
                    struct CultivationPlanResponse: Codable {
                        let html: String
                    }
                    
                    let response = try JSONDecoder().decode(CultivationPlanResponse.self, from: data)
                    print("✅ [研究生-培养方案] 培养方案获取成功（JSON格式），HTML长度: \(response.html.count) 字符")
                    
                    if response.html.isEmpty || response.html.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                        print("❌ [研究生-培养方案] HTML内容为空")
                        completion(.failure(.serverError("培养方案内容为空")))
                    } else {
                        completion(.success(response.html))
                    }
                } catch {
                    print("❌ [研究生-培养方案] 培养方案解析失败: \(error.localizedDescription)")
                    print("❌ [研究生-培养方案] 解析错误详情: \(error)")
                    // 如果所有解析都失败，尝试直接返回字符串
                    if let fallbackString = String(data: data, encoding: .utf8), !fallbackString.isEmpty {
                        print("⚠️ [研究生-培养方案] 使用备用解析方式，直接返回字符串")
                        completion(.success(fallbackString))
                    } else {
                        completion(.failure(.parsingFailed(error.localizedDescription)))
                    }
                }
            }
        }.resume()
    }
}



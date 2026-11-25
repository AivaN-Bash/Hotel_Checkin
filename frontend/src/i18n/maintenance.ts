import type { Language } from "../types/service";

export const maintenanceCopy = {
  en: {
    eyebrow: "THE ASTER HOTEL · GUEST SERVICES",
    title: "We’ll be back shortly.",
    body: "Our check-in service is taking a short pause. Your stay matters to us, and our team is here to help.",
    help: "Please visit the front desk for assistance",
    contact: "Front desk · Contact number to be added",
    checked: "Last checked",
    retry: "Try again",
    returning: "Estimated return",
  },
  th: {
    eyebrow: "โรงแรมเดอะ แอสเตอร์ · บริการผู้เข้าพัก",
    title: "เราจะกลับมาให้บริการเร็ว ๆ นี้",
    body: "ระบบเช็กอินกำลังหยุดให้บริการชั่วคราว เราพร้อมช่วยเหลือคุณตลอดการเข้าพัก",
    help: "กรุณาติดต่อแผนกต้อนรับเพื่อรับความช่วยเหลือ",
    contact: "แผนกต้อนรับ · จะเพิ่มหมายเลขติดต่อภายหลัง",
    checked: "ตรวจสอบล่าสุด",
    retry: "ลองอีกครั้ง",
    returning: "คาดว่าจะกลับมา",
  },
} satisfies Record<Language, Record<string, string>>;
